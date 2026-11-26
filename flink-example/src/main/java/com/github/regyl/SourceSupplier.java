package com.github.regyl;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.util.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;

import com.github.regyl.model.Configs;
import com.github.regyl.util.KafkaOffsetSupplier;


public class SourceSupplier {

    public static KafkaSource<String> getKafkaSource(ParameterTool params) {
        return KafkaSource.<String>builder()
                .setBootstrapServers(params.get(Configs.Kafka.HOSTS))
                .setTopics(params.get(Configs.Kafka.TOPIC))
                .setGroupId(params.get(Configs.Kafka.GROUP_ID))
                .setStartingOffsets(KafkaOffsetSupplier.get(params))
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }
}
