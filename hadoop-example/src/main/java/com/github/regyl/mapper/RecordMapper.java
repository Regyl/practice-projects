package com.github.regyl.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.regyl.util.ConvertUtil;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j 
public class RecordMapper extends SafeMapper<Object, Text, Text, IntWritable> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    @Override
    protected void map0(Object key, Text value, Context context) throws IOException, InterruptedException {
        String val = value.toString();
        JsonNode node = MAPPER.readTree(val);
        if (!node.has("log")) {
            return;
        }
        node = node.get("log");
        node = MAPPER.readTree(node.asText());
        String hostname = ConvertUtil.getHostname(node);
        word.set(hostname);
        context.write(word, one);
    }
}
