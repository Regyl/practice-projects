package com.github.regyl.kafkarabbitmqcomparison.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessagingNamesConstant {

    public static final String TOPIC = "auth.codes";
    public static final String EXCHANGE = "auth.exchange";
    public static final String QUEUE = "auth.codes";
    public static final String ROUTING_KEY = "auth.codes";

    public static final int TOPIC_PARTITIONS = 12;
}
