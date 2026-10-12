package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MqPublisherProxy {

    private Map<Broker, MqPublisher> publishersByBroker;

    public MqPublisherProxy(List<MqPublisher> publishers) {
        publishersByBroker = publishers.stream()
                .collect(Collectors.toUnmodifiableMap(MqPublisher::broker, Function.identity()));
    }

    public MqPublisher get(Broker broker) {
        MqPublisher publisher = publishersByBroker.get(broker);
        if (publisher == null) {
            if (broker == Broker.ALL) {
                throw new IllegalStateException("A single run must target kafka, rabbitmq, redis, or activemq");
            }
            throw new IllegalStateException("Unexpected broker: " + broker);
        }
        return publisher;
    }
}
