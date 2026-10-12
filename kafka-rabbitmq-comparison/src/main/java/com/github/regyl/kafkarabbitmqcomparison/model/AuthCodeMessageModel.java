package com.github.regyl.kafkarabbitmqcomparison.model;

public record AuthCodeMessageModel(
        String id,
        String email,
        String code,
        long sentAtNanos,
        boolean warmup
) {
}
