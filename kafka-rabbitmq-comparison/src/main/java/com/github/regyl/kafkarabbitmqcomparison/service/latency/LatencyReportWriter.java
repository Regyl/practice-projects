package com.github.regyl.kafkarabbitmqcomparison.service.latency;

import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import com.github.regyl.kafkarabbitmqcomparison.model.LatencyReportModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatencyReportWriter {

    private final BenchmarkProperties properties;
    private final JsonMapper jsonMapper;

    public void write(LatencyReportModel report) throws IOException {
        Path directory = Path.of(properties.resultsDir());
        Files.createDirectories(directory);
        String fileName = String.format("%s-%d-eps.json", report.broker(), report.targetEps());
        Path file = directory.resolve(fileName);
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), report);
        log.info("Wrote latency report to {}", file.toAbsolutePath());
    }
}
