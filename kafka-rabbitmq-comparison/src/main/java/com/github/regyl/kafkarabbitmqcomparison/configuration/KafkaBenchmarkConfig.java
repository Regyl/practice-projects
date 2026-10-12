package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaBenchmarkConfig {

    @Bean
    NewTopic authCodesTopic(@Value("${benchmark.listener-concurrency}") int partitions) {
        return TopicBuilder.name(MessagingNamesConstant.TOPIC)
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}
