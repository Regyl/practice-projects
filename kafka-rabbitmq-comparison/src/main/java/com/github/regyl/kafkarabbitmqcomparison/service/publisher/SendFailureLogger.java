package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.Broker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Deprecated
public class SendFailureLogger {

    private final AtomicBoolean logged = new AtomicBoolean();

    public void reset() {
        logged.set(false);
    }

    public void logOnce(Broker broker, Throwable error) {
        if (logged.compareAndSet(false, true)) {
            log.warn("Failed to publish to {}", broker.getWireName(), error);
        }
    }
}
