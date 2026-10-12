package com.github.regyl.kafkarabbitmqcomparison.service.consume;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthCodeRabbitListener {

    private final LatencyRecorder latencyRecorder;

    public AuthCodeRabbitListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @RabbitListener(queues = MessagingNamesConstant.QUEUE)
    public void onMessage(AuthCodeMessageModel message) {
        latencyRecorder.record(Broker.RABBITMQ, message);
    }
}
