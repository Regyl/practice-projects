package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessage;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReport;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatencyRecorderTest {

    @Test
    void summarizesKnownLatencies() {
        LatencyRecorder recorder = new LatencyRecorder();
        recorder.begin(Broker.KAFKA);
        for (int latencyMicros = 1; latencyMicros <= 1_000; latencyMicros++) {
            recorder.recordSent(Broker.KAFKA);
            recorder.recordMeasured(Broker.KAFKA, latencyMicros);
        }

        LatencyReport report = recorder.report(Broker.KAFKA, 1_000, Duration.ofSeconds(1));

        assertEquals("kafka", report.broker());
        assertEquals(1_000, report.targetEps());
        assertEquals(1_000, report.sentCount());
        assertEquals(1_000, report.receivedCount());
        assertEquals(1_000, report.achievedPublishEps(), 0.001);
        assertEquals(1_000, report.achievedConsumeEps(), 0.001);
        assertEquals(1, report.minMicros());
        assertEquals(1_000, report.maxMicros());
        assertTrue(report.p50Micros() >= 450 && report.p50Micros() <= 550, "p50=" + report.p50Micros());
        assertTrue(report.p95Micros() >= 900, "p95=" + report.p95Micros());
        assertTrue(report.p99Micros() >= report.p95Micros(), "p99=" + report.p99Micros());
        assertTrue(report.p999Micros() >= report.p99Micros(), "p999=" + report.p999Micros());
        assertTrue(report.maxMicros() >= report.p999Micros());
    }

    @Test
    void ignoresWarmupMessages() {
        LatencyRecorder recorder = new LatencyRecorder();
        recorder.begin(Broker.RABBITMQ);
        recorder.record(
                Broker.RABBITMQ,
                new AuthCodeMessage("1", "user@example.com", "123456", System.nanoTime(), true)
        );

        LatencyReport report = recorder.report(Broker.RABBITMQ, 1_000, Duration.ofSeconds(1));

        assertEquals(0, report.receivedCount());
        assertEquals(0, report.maxMicros());
    }
}
