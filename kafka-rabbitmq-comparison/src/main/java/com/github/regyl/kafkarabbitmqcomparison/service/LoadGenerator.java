package com.github.regyl.kafkarabbitmqcomparison.service;

import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyReportWriter;
import com.github.regyl.kafkarabbitmqcomparison.service.publisher.MqPublisherProxy;
import com.github.regyl.kafkarabbitmqcomparison.service.publisher.SendFailureLogger;
import com.github.regyl.kafkarabbitmqcomparison.util.SleepUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadGenerator implements ApplicationRunner {

    private final BenchmarkProperties properties;
    private final LatencyRecorder latencyRecorder;
    private final LatencyReportWriter reportWriter;
    private final ShutdownService shutdownService;
    private final SendFailureLogger sendFailureLogger;
    private final MqPublisherProxy publisherProxy;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (Broker broker : selectedBrokers()) {
            for (long eventsPerSecond : properties.eventsPerSecond()) {
                runOne(broker, eventsPerSecond);
                System.gc();
            }
        }
        log.info("Benchmark finished. Reports are in {}", properties.resultsDir());
        shutdownService.shutdown(1);
    }

    private List<Broker> selectedBrokers() {
        if (properties.broker() == Broker.ALL) {
            return List.of(Broker.KAFKA, Broker.RABBITMQ, Broker.REDIS, Broker.ACTIVEMQ);
        }
        return List.of(properties.broker());
    }

    private void runOne(Broker broker, long eventsPerSecond) throws Exception {
        sendFailureLogger.reset();
        publisherProxy.get(broker).purge();
        latencyRecorder.begin(broker);
        log.info(
                "Starting {} benchmark at {} events/sec for {} after {} warmup",
                broker.getWireName(),
                eventsPerSecond,
                properties.duration(),
                properties.warmup()
        );

        long intervalNanos = Math.max(1, 1_000_000_000L / eventsPerSecond);
        long startNanos = System.nanoTime();
        long measureStartNanos = startNanos + properties.warmup().toNanos();
        long endNanos = measureStartNanos + properties.duration().toNanos();
        Semaphore inFlight = new Semaphore(properties.maxInFlight());
        AtomicLong nextSlot = new AtomicLong();
        int threads = properties.publisherThreads();
        ExecutorService workers = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable, "benchmark-publisher-" + broker.getWireName());
            thread.setDaemon(true);
            return thread;
        });
        CountDownLatch finished = new CountDownLatch(threads);
        ScheduledExecutorService progress = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "benchmark-progress");
            thread.setDaemon(true);
            return thread;
        });
        progress.scheduleAtFixedRate(
                () -> logProgress(broker),
                5,
                5,
                TimeUnit.SECONDS
        );

        try {
            for (int i = 0; i < threads; i++) {
                workers.submit(() -> {
                    try {
                        publishLoop(broker, intervalNanos, startNanos, measureStartNanos, endNanos, nextSlot, inFlight);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        log.warn("Publisher thread interrupted for {}", broker.getWireName());
                    } catch (RuntimeException ex) {
                        log.error("Publisher thread failed for {}", broker.getWireName(), ex);
                    } finally {
                        finished.countDown();
                    }
                });
            }
            long awaitSeconds = properties.warmup()
                    .plus(properties.duration())
                    .plus(properties.drainTimeout())
                    .plusSeconds(120)
                    .toSeconds();
            if (!finished.await(awaitSeconds, TimeUnit.SECONDS)) {
                log.warn("Publishers did not finish in time for {}", broker.getWireName());
            }
            drain(broker);
        } finally {
            progress.shutdownNow();
            workers.shutdownNow();
        }

        reportWriter.write(latencyRecorder.report(broker, eventsPerSecond, properties.duration()));
    }

    private void publishLoop(
            Broker broker,
            long intervalNanos,
            long startNanos,
            long measureStartNanos,
            long endNanos,
            AtomicLong nextSlot,
            Semaphore inFlight
    ) throws InterruptedException {
        while (true) {
            long slot = nextSlot.getAndIncrement();
            long targetNanos = startNanos + slot * intervalNanos;
            if (targetNanos >= endNanos) {
                return;
            }
            SleepUtil.sleepUntil(targetNanos);
            inFlight.acquire();
            boolean warmup = targetNanos < measureStartNanos;
            publish(broker, warmup, inFlight);
        }
    }

    private void publish(Broker broker, boolean warmup, Semaphore inFlight) {
        String id = UUID.randomUUID().toString();
        String email = "user-" + id + "@example.com";
        String code = "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
        if (!warmup) {
            latencyRecorder.recordSent(broker);
        }
        long sentAtNanos = System.nanoTime();
        AuthCodeMessageModel message = new AuthCodeMessageModel(id, email, code, sentAtNanos, warmup);
        publisherProxy.get(broker).publish(message, warmup, inFlight);
    }

    private void drain(Broker broker) throws InterruptedException {
        long deadline = System.nanoTime() + properties.drainTimeout().toNanos();
        while (latencyRecorder.receivedCount(broker) < latencyRecorder.sentCount(broker)
                && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        long received = latencyRecorder.receivedCount(broker);
        long sent = latencyRecorder.sentCount(broker);
        if (received < sent) {
            log.warn("Drain timed out for {} with {} of {} measured messages received", broker.getWireName(), received, sent);
        }
    }

    private void logProgress(Broker broker) {
        try {
            log.info(
                    "{} progress sent={} received={}",
                    broker.getWireName(),
                    latencyRecorder.sentCount(broker),
                    latencyRecorder.receivedCount(broker)
            );
        } catch (RuntimeException ex) {
            log.warn("Progress log failed", ex);
        }
    }

}
