package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
public class KafkaMqPublisher implements MqPublisher {

    private final KafkaTemplate<String, AuthCodeMessageModel> kafkaTemplate;
    private final LatencyRecorder latencyRecorder;
    private final SendFailureLogger sendFailureLogger;

    @Override
    public Broker broker() {
        return Broker.KAFKA;
    }

    @Override
    public void publish(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            kafkaTemplate.send(MessagingNamesConstant.TOPIC, message.id(), message).whenComplete((result, error) -> {
                inFlight.release();
                if (error != null) {
                    if (!message.warmup()) {
                        latencyRecorder.recordSendFailure(Broker.KAFKA);
                    }
                    sendFailureLogger.logOnce(Broker.KAFKA, error);
                }
            });
        } catch (RuntimeException ex) {
            inFlight.release();
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.KAFKA);
            }
            sendFailureLogger.logOnce(Broker.KAFKA, ex);
        }
    }

    @Override
    public void purge() {

    }
}
