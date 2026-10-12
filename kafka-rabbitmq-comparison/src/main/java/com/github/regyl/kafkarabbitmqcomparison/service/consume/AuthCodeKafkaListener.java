package com.github.regyl.kafkarabbitmqcomparison.service.consume;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessage;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNames;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthCodeKafkaListener {

    private final LatencyRecorder latencyRecorder;

    public AuthCodeKafkaListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @KafkaListener(topics = MessagingNames.TOPIC)
    public void onMessage(AuthCodeMessage message) {
        latencyRecorder.record(Broker.KAFKA, message);
    }
}
