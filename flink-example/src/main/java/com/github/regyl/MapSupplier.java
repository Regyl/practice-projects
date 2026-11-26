package com.github.regyl;

import java.util.UUID;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.regyl.util.ConvertUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class MapSupplier implements MapFunction<String, Row> {

    public static final TypeInformation<Row> ROW_TYPE =
            Types.ROW_NAMED(new String[] {"id", "hostname", "timestamp", "log"}, Types.STRING, Types.STRING, Types.INT, Types.STRING);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Row map(String item) throws Exception {
        log.info(item);
        Row row = new Row(RowKind.INSERT, 4);
        JsonNode node = MAPPER.readTree(item);
        int timestamp = node.get("@timestamp").asInt();
        String hostname = ConvertUtil.getHostname(node);
        String id = String.format("%d-%s-%s", timestamp, hostname, UUID.randomUUID().toString());
        row.setField(0, id);
        row.setField(1, hostname);
        row.setField(2, timestamp);
        row.setField(3, item);
        return row;
    }

    

}
