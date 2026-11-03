package com.deepspace;

import com.deepspace.model.Configs;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;


public class SourceSupplier {

    public static KafkaSource<String> getKafkaSource(ParameterTool params) {
        return KafkaSource.<String>builder()
                .setBootstrapServers(params.get(Configs.Kafka.HOSTS))
                .setTopics(params.get(Configs.Kafka.TOPIC))
                .setGroupId(params.get(Configs.Kafka.GROUP_ID))
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }
}
