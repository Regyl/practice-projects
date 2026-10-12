package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Duration;

@ConfigurationProperties(prefix = "benchmark")
public record BenchmarkProperties(
        Broker broker,
        long eventsPerSecond,
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
        if (eventsPerSecond <= 0) {
            eventsPerSecond = 1_000;
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
