package com.github.regyl.kafkarabbitmqcomparison.service.consumer;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.stereotype.Component;

@Component
public class RedisMqListener implements MqListener {

    private final LatencyRecorder latencyRecorder;

    public RedisMqListener(LatencyRecorder latencyRecorder) {
        this.latencyRecorder = latencyRecorder;
    }

    @Override
    @RedisListener(topic = MessagingNamesConstant.CHANNEL, consumes = "application/json")
    public void onMessage(AuthCodeMessageModel message) {
        latencyRecorder.record(Broker.REDIS, message);
    }
}
