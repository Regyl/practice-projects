package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReport;
import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LatencyReportWriterTest {

    @Test
    void writesSummaryJson(@TempDir Path directory) throws Exception {
        BenchmarkProperties properties = new BenchmarkProperties(
                false,
                Broker.KAFKA,
                1_000,
                Duration.ofSeconds(30),
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                1,
                50_000,
                directory.toString()
        );
        LatencyReport report = new LatencyReport(
                "kafka",
                1_000,
                998.2,
                997.1,
                29_946,
                29_913,
                120,
                450.5,
                400,
                900,
                1_500,
                4_000,
                8_000
        );

        Path file = new LatencyReportWriter(properties, JsonMapper.builder().build()).write(report);
        String json = Files.readString(file);

        assertTrue(file.getFileName().toString().startsWith("kafka-1000eps-"));
        assertTrue(file.getFileName().toString().endsWith(".json"));
        assertTrue(json.contains("\"broker\" : \"kafka\"") || json.contains("\"broker\": \"kafka\""));
        assertTrue(json.contains("1500"));
        assertTrue(json.contains("29946"));
    }
}
