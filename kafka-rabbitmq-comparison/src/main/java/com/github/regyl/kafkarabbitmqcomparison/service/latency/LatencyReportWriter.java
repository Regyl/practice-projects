package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReport;
import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class LatencyReportWriter {

    private static final Logger log = LoggerFactory.getLogger(LatencyReportWriter.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    private final BenchmarkProperties properties;
    private final JsonMapper jsonMapper;

    public LatencyReportWriter(BenchmarkProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    public Path write(LatencyReport report) throws IOException {
        Path directory = Path.of(properties.resultsDir());
        Files.createDirectories(directory);
        String timestamp = TIMESTAMP.format(LocalDateTime.now());
        Path file = directory.resolve(report.broker() + "-" + report.targetEps() + "eps-" + timestamp + ".json");
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), report);
        log.info("Wrote latency report to {}", file.toAbsolutePath());
        log.info(
                "{} targetEps={} publishEps={} consumeEps={} sent={} received={} min={}us mean={}us p50={}us p95={}us p99={}us p999={}us max={}us",
                report.broker(),
                report.targetEps(),
                format(report.achievedPublishEps()),
                format(report.achievedConsumeEps()),
                report.sentCount(),
                report.receivedCount(),
                report.minMicros(),
                format(report.meanMicros()),
                report.p50Micros(),
                report.p95Micros(),
                report.p99Micros(),
                report.p999Micros(),
                report.maxMicros()
        );
        return file;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
