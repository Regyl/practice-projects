package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "benchmark")
public record BenchmarkProperties(
        Broker broker,
        List<Long> eventsPerSecond,
        Duration duration,
        Duration warmup,
        Duration drainTimeout,
        int publisherThreads,
        int maxInFlight,
        String resultsDir
) {

    public BenchmarkProperties {
        if (broker == null) {
            broker = Broker.KAFKA;
        }
        if (CollectionUtils.isEmpty(eventsPerSecond)) {
            eventsPerSecond = List.of(1_000L);
        } else {
            for (Long rate : eventsPerSecond) {
                if (rate == null || rate <= 0) {
                    throw new IllegalArgumentException("benchmark.events-per-second values must be positive");
                }
            }
            eventsPerSecond = List.copyOf(eventsPerSecond);
        }
        if (duration == null || duration.isZero() || duration.isNegative()) {
            duration = Duration.ofSeconds(30);
        }
        if (warmup == null || warmup.isNegative()) {
            warmup = Duration.ofSeconds(5);
        }
        if (drainTimeout == null || drainTimeout.isNegative()) {
            drainTimeout = Duration.ofSeconds(30);
        }
        if (publisherThreads <= 0) {
            publisherThreads = 1;
        }
        if (maxInFlight <= 0) {
            maxInFlight = 50_000;
        }
        if (!StringUtils.hasLength(resultsDir)) {
            resultsDir = "results";
        }
    }
}
