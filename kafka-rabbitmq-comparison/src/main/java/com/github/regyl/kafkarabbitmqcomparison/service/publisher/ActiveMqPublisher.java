package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import com.github.regyl.kafkarabbitmqcomparison.service.latency.LatencyRecorder;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveMqPublisher implements MqPublisher {

    private final JmsTemplate jmsTemplate;
    private final LatencyRecorder latencyRecorder;
    private final SendFailureLogger sendFailureLogger;

    @Override
    public Broker broker() {
        return Broker.ACTIVEMQ;
    }

    @Override
    public void publish(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight) {
        try {
            jmsTemplate.convertAndSend(MessagingNamesConstant.ACTIVEMQ_QUEUE, message);
        } catch (RuntimeException ex) {
            if (!warmup) {
                latencyRecorder.recordSendFailure(Broker.ACTIVEMQ);
            }
            sendFailureLogger.logOnce(Broker.ACTIVEMQ, ex);
        } finally {
            inFlight.release();
        }
    }

    @Override
    public void purge() {
        try {
            jmsTemplate.execute(session -> {
                Queue queue = session.createQueue(MessagingNamesConstant.ACTIVEMQ_QUEUE);
                try (MessageConsumer consumer = session.createConsumer(queue)) {
                    Message leftover = consumer.receiveNoWait();
                    while (leftover != null) {
                        leftover = consumer.receiveNoWait();
                    }
                }
                return null;
            }, true);
        } catch (RuntimeException ex) {
            log.warn(
                    "Could not purge ActiveMQ queue {} before the run",
                    MessagingNamesConstant.ACTIVEMQ_QUEUE,
                    ex
            );
        }
    }


}
