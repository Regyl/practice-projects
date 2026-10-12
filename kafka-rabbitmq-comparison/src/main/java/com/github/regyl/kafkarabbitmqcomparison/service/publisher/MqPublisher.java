package com.github.regyl.kafkarabbitmqcomparison.service.publisher;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import com.github.regyl.kafkarabbitmqcomparison.model.Broker;

import java.util.concurrent.Semaphore;

public interface MqPublisher {

    Broker broker();

    void publish(AuthCodeMessageModel message, boolean warmup, Semaphore inFlight);

    void purge();
}
