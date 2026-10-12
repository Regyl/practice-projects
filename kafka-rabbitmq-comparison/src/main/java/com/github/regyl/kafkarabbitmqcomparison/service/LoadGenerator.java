package com.github.regyl.kafkarabbitmqcomparison.service;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyReportWriter;
import com.github.regyl.kafkarabbitmqcomparison.util.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(prefix = "benchmark", name = "enabled", havingValue = "true")
public class LoadGenerator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadGenerator.class);

    private final BenchmarkProperties properties;
    private final LatencyRecorder latencyRecorder;
    private final LatencyReportWriter reportWriter;
    private final KafkaTemplate<String, AuthCodeMessageModel> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final AtomicBoolean sendFailureLogged = new AtomicBoolean();

    public LoadGenerator(
            BenchmarkProperties properties,
            LatencyRecorder latencyRecorder,
            LatencyReportWriter reportWriter,
            KafkaTemplate<String, AuthCodeMessageModel> kafkaTemplate,
            RabbitTemplate rabbitTemplate
    ) {
        this.properties = properties;
        this.latencyRecorder = latencyRecorder;
        this.reportWriter = reportWriter;
        this.kafkaTemplate = kafkaTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (Broker broker : selectedBrokers()) {
            runOne(broker);
        }
        log.info("Benchmark finished. Reports are in {}", properties.resultsDir());
        System.exit(1);
    }

    private List<Broker> selectedBrokers() {
        if (properties.broker() == Broker.ALL) {
            return List.of(Broker.KAFKA, Broker.RABBITMQ);
        }
        return List.of(properties.broker());
    }

    private void runOne(Broker broker) throws Exception {
        sendFailureLogged.set(false);
        if (broker == Broker.RABBITMQ) {
            purgeRabbitQueue();
        }
        latencyRecorder.begin(broker);
        log.info(
                "Starting {} benchmark at {} events/sec for {} after {} warmup",
                broker.wireName(),
                properties.eventsPerSecond(),
                properties.duration(),
                properties.warmup()
        );

        long intervalNanos = Math.max(1, 1_000_000_000L / properties.eventsPerSecond());
        long startNanos = System.nanoTime();
        long measureStartNanos = startNanos + properties.warmup().toNanos();
        long endNanos = measureStartNanos + properties.duration().toNanos();
        Semaphore inFlight = new Semaphore(properties.maxInFlight());
        AtomicLong nextSlot = new AtomicLong();
        int threads = properties.publisherThreads();
        ExecutorService workers = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable, "benchmark-publisher-" + broker.wireName());
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
                        log.warn("Publisher thread interrupted for {}", broker.wireName());
                    } catch (RuntimeException ex) {
                        log.error("Publisher thread failed for {}", broker.wireName(), ex);
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
                log.warn("Publishers did not finish in time for {}", broker.wireName());
            }
            drain(broker);
        } finally {
            progress.shutdownNow();
            workers.shutdownNow();
        }

        reportWriter.write(latencyRecorder.report(broker, properties.eventsPerSecond(), properties.duration()));
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
        switch (broker) {
            case KAFKA -> publishKafka(message, warmup, inFlight);
            case RABBITMQ -> publishRabbit(message, warmup, inFlight);
            case ALL -> throw new IllegalStateException("A single run must target kafka or rabbitmq");
            default -> throw new IllegalStateException("Unexpected broker: " + broker);
        }
    }

    private void publishKafka(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            kafkaTemplate.send(MessagingNamesConstant.TOPIC, message.id(), message).whenComplete((result, error) -> {
                inFlight.release();
                if (error != null) {
                    if (!message.warmup()) {
                        latencyRecorder.recordSendFailure(Broker.KAFKA);
                    }
                    logSendFailure(Broker.KAFKA, error);
                }
            });
        } catch (RuntimeException ex) {
            inFlight.release();
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.KAFKA);
            }
            logSendFailure(Broker.KAFKA, ex);
        }
    }

    private void publishRabbit(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            rabbitTemplate.convertAndSend(MessagingNamesConstant.EXCHANGE, MessagingNamesConstant.ROUTING_KEY, message);
        } catch (RuntimeException ex) {
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.RABBITMQ);
            }
            logSendFailure(Broker.RABBITMQ, ex);
        } finally {
            inFlight.release();
        }
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
            log.warn("Drain timed out for {} with {} of {} measured messages received", broker.wireName(), received, sent);
        }
    }

    private void purgeRabbitQueue() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queuePurge(MessagingNamesConstant.QUEUE);
                return null;
            });
        } catch (RuntimeException ex) {
            log.warn("Could not purge RabbitMQ queue {} before the run", MessagingNamesConstant.QUEUE, ex);
        }
    }

    private void logProgress(Broker broker) {
        try {
            log.info(
                    "{} progress sent={} received={}",
                    broker.wireName(),
                    latencyRecorder.sentCount(broker),
                    latencyRecorder.receivedCount(broker)
            );
        } catch (RuntimeException ex) {
            log.warn("Progress log failed", ex);
        }
    }

    private void logSendFailure(Broker broker, Throwable error) {
        if (sendFailureLogged.compareAndSet(false, true)) {
            log.warn("Failed to publish to {}", broker.wireName(), error);
        }
    }
}
