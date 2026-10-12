package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessage;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReport;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Component
public class LatencyRecorder {

    static final long MAX_MICROS = TimeUnit.SECONDS.toMicros(60);

    private final ConcurrentMap<Broker, Recorder> histograms = new ConcurrentHashMap<>();
    private final ConcurrentMap<Broker, LongAdder> sentCounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Broker, LongAdder> receivedCounts = new ConcurrentHashMap<>();

    public void begin(Broker broker) {
        histograms.put(broker, new Recorder(1, MAX_MICROS, 3));
        sentCounts.put(broker, new LongAdder());
        receivedCounts.put(broker, new LongAdder());
    }

    public void recordSent(Broker broker) {
        sentCounter(broker).increment();
    }

    public void recordSendFailure(Broker broker) {
        sentCounter(broker).decrement();
    }

    public void record(Broker broker, AuthCodeMessage message) {
        if (message.warmup()) {
            return;
        }
        long latencyNanos = Math.max(0, System.nanoTime() - message.sentAtNanos());
        recordMeasured(broker, TimeUnit.NANOSECONDS.toMicros(latencyNanos));
    }

    void recordMeasured(Broker broker, long latencyMicros) {
        histogram(broker).recordValue(clampMicros(latencyMicros));
        receivedCounter(broker).increment();
    }

    public long sentCount(Broker broker) {
        return sentCounter(broker).sum();
    }

    public long receivedCount(Broker broker) {
        return receivedCounter(broker).sum();
    }

    public LatencyReport report(Broker broker, long targetEps, Duration duration) {
        Histogram histogram = histogram(broker).getIntervalHistogram();
        long sent = sentCounter(broker).sum();
        long received = receivedCounter(broker).sum();
        double seconds = Math.max(duration.toNanos(), 1) / 1_000_000_000d;
        if (histogram.getTotalCount() == 0) {
            return empty(broker, targetEps, sent, received, seconds);
        }
        return new LatencyReport(
                broker.wireName(),
                targetEps,
                sent / seconds,
                received / seconds,
                sent,
                received,
                histogram.getMinValue(),
                histogram.getMean(),
                histogram.getValueAtPercentile(50),
                histogram.getValueAtPercentile(95),
                histogram.getValueAtPercentile(99),
                histogram.getValueAtPercentile(99.9),
                histogram.getMaxValue()
        );
    }

    private static LatencyReport empty(Broker broker, long targetEps, long sent, long received, double seconds) {
        return new LatencyReport(
                broker.wireName(),
                targetEps,
                sent / seconds,
                received / seconds,
                sent,
                received,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    private Recorder histogram(Broker broker) {
        return histograms.computeIfAbsent(broker, ignored -> new Recorder(1, MAX_MICROS, 3));
    }

    private LongAdder sentCounter(Broker broker) {
        return sentCounts.computeIfAbsent(broker, ignored -> new LongAdder());
    }

    private LongAdder receivedCounter(Broker broker) {
        return receivedCounts.computeIfAbsent(broker, ignored -> new LongAdder());
    }

    private static long clampMicros(long latencyMicros) {
        if (latencyMicros < 1) {
            return 1;
        }
        if (latencyMicros > MAX_MICROS) {
            return MAX_MICROS;
        }
        return latencyMicros;
    }
}
