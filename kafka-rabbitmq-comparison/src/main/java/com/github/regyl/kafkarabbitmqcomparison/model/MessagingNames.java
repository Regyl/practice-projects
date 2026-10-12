package com.github.regyl.kafkarabbitmqcomparison.model;

public final class MessagingNames {

    public static final String TOPIC = "auth.codes";
    public static final String EXCHANGE = "auth.exchange";
    public static final String QUEUE = "auth.codes";
    public static final String ROUTING_KEY = "auth.codes";

    public static final int TOPIC_PARTITIONS = 12;

    private MessagingNames() {
    }
}
