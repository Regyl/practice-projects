package com.github.regyl;

import com.github.regyl.model.Configs;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.util.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

@Slf4j
public class Main {

    private static final String JOB_NAME = "Fluent Bit Job";

    public static void main(String[] args) throws Exception {
        log.info("Starting " + JOB_NAME);
        String propertiesFilePath = args.length == 0 ? "src/main/resources/conf.properties" : args[0];
        ParameterTool params = ParameterTool.fromPropertiesFile(propertiesFilePath);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(params.getInt(Configs.Default.PARALLELISM));

        KafkaSource<String> source = SourceSupplier.getKafkaSource(params);
        DataStream<String> dataStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka");
        DataStream<Row> mapped = dataStream.map(new MapSupplier()).returns(MapSupplier.ROW_TYPE);

        mapped.sinkTo(SinkSupplier.getCassandraSink(params)).name("Cassandra");

        env.execute(JOB_NAME);
    }
}
