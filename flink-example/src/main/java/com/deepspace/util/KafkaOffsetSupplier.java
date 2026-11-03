package com.deepspace.util;

import lombok.experimental.UtilityClass;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

@UtilityClass
public class KafkaOffsetSupplier {

    public OffsetsInitializer get(ParameterTool params) {
        String offset = params.get("source.kafka.offset");
        if ("earliest".equals(offset)) {
            return OffsetsInitializer.earliest();
        } else if ("latest".equals(offset)) {
            return OffsetsInitializer.latest();
        } else  {
            throw new IllegalArgumentException(String.format("Unknown offset %s", offset));
        }
    }
}
