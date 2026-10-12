package com.github.regyl.kafkarabbitmqcomparison.service.consume;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessage;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNames;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthCodeRabbitListener {

    private final LatencyRecorder latencyRecorder;

    public AuthCodeRabbitListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @RabbitListener(queues = MessagingNames.QUEUE)
    public void onMessage(AuthCodeMessage message) {
        latencyRecorder.record(Broker.RABBITMQ, message);
    }
}
