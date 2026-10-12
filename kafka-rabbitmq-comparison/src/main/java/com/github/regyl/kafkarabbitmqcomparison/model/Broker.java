package com.github.regyl.kafkarabbitmqcomparison.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Broker {

    KAFKA("kafka"),
    RABBITMQ("rabbitmq"),
    REDIS("redis"),
    ACTIVEMQ("activemq"),
    ALL("all");

    private final String wireName;
}
