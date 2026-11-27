package com.github.regyl.reducer;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
public abstract class SafeReducer<K1, V1, K2, V2> extends Reducer<K1, V1, K2, V2> {

    @Override
    public void reduce(K1 key, Iterable<V1> values, Context context) {
        try {
            reduce0(key, values, context);
        } catch (Exception e) {
            String msg = String.format("[%s]", e.getMessage());
            log.error(msg);
            throw new RuntimeException(e);
        }
    }

    protected abstract void reduce0(K1 key, Iterable<V1> values, Context context) throws IOException, InterruptedException;
}
