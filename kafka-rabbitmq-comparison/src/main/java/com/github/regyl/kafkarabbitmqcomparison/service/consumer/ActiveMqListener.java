package com.github.regyl.kafkarabbitmqcomparison.service.consumer;

import com.github.regyl.kafkarabbitmqcomparison.annotation.ActiveMqEnabled;
import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@ActiveMqEnabled
public class ActiveMqListener implements MqListener {

    private final LatencyRecorder latencyRecorder;

    public ActiveMqListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @Override
    @JmsListener(destination = MessagingNamesConstant.ACTIVEMQ_QUEUE)
    public void onMessage(AuthCodeMessageModel message) {
        latencyRecorder.record(Broker.ACTIVEMQ, message);
    }
}
