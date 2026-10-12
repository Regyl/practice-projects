package com.github.regyl.kafkarabbitmqcomparison.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SleepUtil {

    public static void sleepUntil(long targetNanos) throws InterruptedException {
        long remaining;
        while ((remaining = targetNanos - System.nanoTime()) > 0) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            if (remaining > 2_000_000L) {
                Thread.sleep(Math.min(remaining / 1_000_000L - 1, 50));
            } else {
                Thread.onSpinWait();
            }
        }
    }
}
