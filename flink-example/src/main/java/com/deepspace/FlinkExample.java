package com.deepspace;

import com.deepspace.model.Configs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

@Slf4j
public class FlinkExample {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static void main(String[] args) throws Exception {
        log.info("Starting Flink Example");
        String propertiesFilePath = "flink-example/src/main/resources/conf.properties";
        ParameterTool params = ParameterTool.fromPropertiesFile(propertiesFilePath);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(params.getInt(Configs.Default.PARALLELISM));

        KafkaSource<String> source = SourceSupplier.getKafkaSource(params);
        DataStream<String> dataStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka");
        DataStream<Row> mapped = dataStream.map(item -> {
            Row row = new Row(RowKind.INSERT, 2);
            JsonNode node = MAPPER.readTree(item);
            row.setField(0, node.get("@timestamp").asInt());
            row.setField(1, node.get("rand_value").asText());
            return row;
        });

        SinkSupplier.getCassandraSink(mapped, params);

        env.execute("Flink Example Job");
    }
}
