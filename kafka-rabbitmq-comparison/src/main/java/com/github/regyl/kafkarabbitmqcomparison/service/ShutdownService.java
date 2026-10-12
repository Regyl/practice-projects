package com.github.regyl.kafkarabbitmqcomparison.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShutdownService {

    @Autowired
    private ApplicationContext context;

    public void shutdown(int exitCode) {
        // Triggers Spring context cleanup and safely terminates the JVM
        int finalCode = SpringApplication.exit(context, () -> exitCode);
        System.exit(finalCode);
    }
}
