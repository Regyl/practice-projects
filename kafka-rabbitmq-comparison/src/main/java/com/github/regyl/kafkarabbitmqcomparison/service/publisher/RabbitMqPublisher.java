package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqPublisher implements MqPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final LatencyRecorder latencyRecorder;
    private final SendFailureLogger sendFailureLogger;

    @Override
    public Broker broker() {
        return Broker.RABBITMQ;
    }

    @Override
    public void publish(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            rabbitTemplate.convertAndSend(
                    MessagingNamesConstant.EXCHANGE,
                    MessagingNamesConstant.ROUTING_KEY,
                    message
            );
        } catch (RuntimeException ex) {
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.RABBITMQ);
            }
            sendFailureLogger.logOnce(Broker.RABBITMQ, ex);
        } finally {
            inFlight.release();
        }
    }

    @Override
    public void purge() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queuePurge(MessagingNamesConstant.QUEUE);
                return null;
            });
        } catch (RuntimeException ex) {
            log.warn("Could not purge RabbitMQ queue {} before the run", MessagingNamesConstant.QUEUE, ex);
        }
    }
}
