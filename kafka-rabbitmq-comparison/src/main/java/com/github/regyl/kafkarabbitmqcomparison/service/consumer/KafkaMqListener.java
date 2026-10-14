package com.github.regyl.kafkarabbitmqcomparison.service.consumer;

import com.github.regyl.kafkarabbitmqcomparison.annotation.KafkaMqEnabled;
import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaMqEnabled
public class KafkaMqListener implements MqListener {

    private final LatencyRecorder latencyRecorder;

    public KafkaMqListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @Override
    @KafkaListener(topics = MessagingNamesConstant.TOPIC)
    public void onMessage(AuthCodeMessageModel message) {
        latencyRecorder.record(Broker.KAFKA, message);
    }
}
