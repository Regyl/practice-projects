package com.github.regyl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoubleBraceLeakDemo {

    // Simulates a long-lived application cache.
    private static final List<Set<String>> CACHE = new ArrayList<>();

    static class LeakyService {
        // Simulates a large object owned by the service.
        byte[] payload = new byte[20 * 1024 * 1024];

        Set<String> countries = new HashSet<String>() {{
            add("India");
            add("USA");

            // Explicitly accesses the enclosing instance.
            // This ensures the anonymous subclass retains it.
            add("Payload size: " + payload.length);
        }};
    }

    static class SafeService {
        byte[] payload = new byte[20 * 1024 * 1024];

        Set<String> countries = new HashSet<>();

        SafeService() {
            countries.add("India");
            countries.add("USA");
            countries.add("Payload size: " + payload.length);
        }
    }

    private static WeakReference<?> createLeakyService() {
        LeakyService service = new LeakyService();
        CACHE.add(service.countries);

        return new WeakReference<>(service);
    }

    private static WeakReference<?> createSafeService() {
        SafeService service = new SafeService();
        CACHE.add(service.countries);

        return new WeakReference<>(service);
    }

    private static void awaitCollection(
            WeakReference<?> reference) throws InterruptedException {

        for (int i = 0; i < 50 && reference.get() != null; i++) {
            System.gc();
            Thread.sleep(100);
        }

        System.out.println(
            reference.get() == null
                ? "Service was collected"
                : "Service is still reachable"
        );
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Double-brace version:");
        WeakReference<?> leaky = createLeakyService();
        awaitCollection(leaky);

        CACHE.clear();
        System.gc();
        Thread.sleep(500);

        System.out.println("\nNormal HashSet version:");
        WeakReference<?> safe = createSafeService();
        awaitCollection(safe);
    }
}
