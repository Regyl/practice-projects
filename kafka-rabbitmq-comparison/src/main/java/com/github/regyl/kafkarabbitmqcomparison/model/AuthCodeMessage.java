package com.github.regyl.kafkarabbitmqcomparison.model;

public record AuthCodeMessage(
        String id,
        String email,
        String code,
        long sentAtNanos,
        boolean warmup
) {
}
