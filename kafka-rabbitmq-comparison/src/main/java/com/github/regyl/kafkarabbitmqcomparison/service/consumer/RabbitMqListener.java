package com.github.regyl.kafkarabbitmqcomparison.service.consumer;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqListener implements MqListener {

    private final LatencyRecorder latencyRecorder;

    public RabbitMqListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @Override
    @RabbitListener(queues = MessagingNamesConstant.QUEUE)
    public void onMessage(AuthCodeMessageModel message) {
        latencyRecorder.record(Broker.RABBITMQ, message);
    }
}
