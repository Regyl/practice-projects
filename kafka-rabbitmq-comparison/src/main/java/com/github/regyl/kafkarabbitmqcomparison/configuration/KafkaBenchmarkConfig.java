package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaBenchmarkConfig {

    @Bean
    NewTopic authCodesTopic() {
        return TopicBuilder.name(MessagingNamesConstant.TOPIC)
                .partitions(MessagingNamesConstant.TOPIC_PARTITIONS)
                .replicas(1)
                .build();
    }
}
