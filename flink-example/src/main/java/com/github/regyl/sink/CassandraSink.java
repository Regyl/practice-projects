package com.github.regyl.sink;

import java.net.InetSocketAddress;

import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.types.Row;

public record CassandraSink(String host, int port, String localDatacenter, String query)
        implements Sink<Row> {

    @Override
    public SinkWriter<Row> createWriter(WriterInitContext context) {
        CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(localDatacenter)
                .build();
        try {
            return new CassandraWriter(session, session.prepare(query));
        } catch (RuntimeException failure) {
            try {
                session.close();
            } catch (RuntimeException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }
}
