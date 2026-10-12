package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
public class RedisMqPublisher implements MqPublisher {

    private final LatencyRecorder latencyRecorder;
    private final SendFailureLogger sendFailureLogger;
    @Qualifier("authCodeRedisTemplate")
    private final RedisTemplate<String, AuthCodeMessageModel> authCodeRedisTemplate;

    @Override
    public Broker broker() {
        return Broker.REDIS;
    }

    @Override
    public void publish(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            Long receivers = authCodeRedisTemplate.convertAndSend(MessagingNamesConstant.CHANNEL, message);
            if (receivers == null || receivers == 0) {
                if (!warmup) {
                    latencyRecorder.recordSendFailure(Broker.REDIS);
                }
                sendFailureLogger.logOnce(Broker.REDIS, new IllegalStateException("Redis publish reached no subscribers"));
            }
        } catch (RuntimeException ex) {
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.REDIS);
            }
            sendFailureLogger.logOnce(Broker.REDIS, ex);
        } finally {
            inFlight.release();
        }
    }

    @Override
    public void purge() {

    }
}
