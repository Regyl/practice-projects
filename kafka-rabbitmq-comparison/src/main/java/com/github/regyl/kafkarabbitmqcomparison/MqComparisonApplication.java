package com.github.regyl.kafkarabbitmqcomparison;

import com.github.regyl.kafkarabbitmqcomparison.configuration.BenchmarkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BenchmarkProperties.class)
public class MqComparisonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqComparisonApplication.class, args);
    }

}
