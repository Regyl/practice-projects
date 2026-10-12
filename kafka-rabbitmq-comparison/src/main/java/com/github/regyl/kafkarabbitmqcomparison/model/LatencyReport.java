package com.github.regyl.kafkarabbitmqcomparison.model;

public record LatencyReport(
        String broker,
        long targetEps,
        double achievedPublishEps,
        double achievedConsumeEps,
        long sentCount,
        long receivedCount,
        long minMicros,
        double meanMicros,
        long p50Micros,
        long p95Micros,
        long p99Micros,
        long p999Micros,
        long maxMicros
) {
}
