package com.github.regyl.kafkarabbitmqcomparison.model;

public enum Broker {

    KAFKA("kafka"),
    RABBITMQ("rabbitmq"),
    REDIS("redis"),
    ALL("all");

    private final String wireName;

    Broker(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}
