package com.github.regyl;

import com.github.regyl.sink.CassandraSink;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.types.Row;
import org.apache.flink.util.ParameterTool;

public class SinkSupplier {

    public static Sink<Row> getCassandraSink(ParameterTool params) {
        return new CassandraSink(
                params.getRequired("sink.cassandra.host"),
                params.getInt("sink.cassandra.port"),
                params.get("sink.cassandra.local-datacenter", "datacenter1"),
                params.getRequired("sink.cassandra.query"));
    }
}
