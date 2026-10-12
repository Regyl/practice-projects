package com.github.regyl.kafkarabbitmqcomparison.service.consumer;

import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;

public interface MqListener {

    void onMessage(AuthCodeMessageModel message);
}
