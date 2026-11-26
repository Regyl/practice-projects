package com.github.regyl.sink;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.types.Row;

record CassandraWriter(CqlSession session, PreparedStatement statement)
        implements SinkWriter<Row> {

    @Override
    public void write(Row row, Context context) {
        session.execute(statement.bind(
                row.getField(0), row.getField(1), row.getField(2), row.getField(3)));
    }

    @Override
    public void flush(boolean endOfInput) {
        // A synchronous write leaves no pending requests to flush.
    }

    @Override
    public void close() {
        session.close();
    }
}
