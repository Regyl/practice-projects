package com.github.regyl.mapper;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;


import lombok.extern.slf4j.Slf4j;

@Slf4j 
public abstract class SafeMapper<K1, V1, K2, V2> extends Mapper<K1, V1, K2, V2> {

    @Override
    public void map(K1 key, V1 value, Context context) {
        try {
            map0(key, value, context);
        } catch (Exception e) {
            String msg = String.format("[%s][%s]", e.getMessage(), value.toString());
            log.error(msg);
            throw new RuntimeException(e);
        }
    }

    protected abstract void map0(K1 key, V1 value, Context context) throws IOException, InterruptedException;

}
