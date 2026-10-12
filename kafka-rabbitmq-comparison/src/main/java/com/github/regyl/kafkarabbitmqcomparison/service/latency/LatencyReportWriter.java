package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReportModel;
import com.github.regyl.kafkarabbitmqcomparison.util.FormatUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class LatencyReportWriter {

    private static final Logger log = LoggerFactory.getLogger(LatencyReportWriter.class);

    private final BenchmarkProperties properties;
    private final JsonMapper jsonMapper;

    public void write(LatencyReportModel report) throws IOException {
        Path directory = Path.of(properties.resultsDir());
        Files.createDirectories(directory);
        Path file = directory.resolve(report.broker() + "-" + report.targetEps() + "eps.json");
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), report);
        log.info("Wrote latency report to {}", file.toAbsolutePath());
        log.info(
                "{} targetEps={} publishEps={} consumeEps={} sent={} received={}"
                        + " min={}us mean={}us p50={}us p95={}us p99={}us p999={}us max={}us",
                report.broker(),
                report.targetEps(),
                FormatUtils.format(report.achievedPublishEps()),
                FormatUtils.format(report.achievedConsumeEps()),
                report.sentCount(),
                report.receivedCount(),
                report.minMicros(),
                FormatUtils.format(report.meanMicros()),
                report.p50Micros(),
                report.p95Micros(),
                report.p99Micros(),
                report.p999Micros(),
                report.maxMicros()
        );
    }
}
