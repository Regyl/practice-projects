package com.deepspace.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Configs {

    public static final class Default {

        public static final String PARALLELISM = "parallelism.default";
    }

    public static final class Kafka {

        public static final String HOSTS = "source.kafka.bootstrap-servers";

        public static final String TOPIC = "source.kafka.topic";

        public static final String GROUP_ID = "source.kafka.groupid";
    }

    public static final class Clickhouse {
        public static final String HOSTS = "sink.clickhouse.hosts";

        public static final String USERNAME = "sink.clickhouse.username";
        public static final String PASSWORD = "sink.clickhouse.password";
        public static final String DATABASE = "sink.clickhouse.database";
        public static final String TABLE_NAME = "sink.clickhouse.table-name";
    }
}
