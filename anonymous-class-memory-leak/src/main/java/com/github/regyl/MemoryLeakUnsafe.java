package com.github.regyl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryLeakUnsafe {
    //simulate a long-living application
    static List<Set<String>> cache = new ArrayList<>();

    static class Service {
        byte[] data = new byte[100 * 1024 * 1024]; // 100 MB

        Set<String> countries = new HashSet<>() {{
            add("USA");
        }};
    }

    static void createService() {
        Service service = new Service();
        cache.add(service.countries);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            createService();
        }

        System.gc();
        Thread.sleep(1000);

//        MemoryUtil.printUsed(); //1022 MB
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("Approx. used heap: %.2f MB%n",
                used / (1024.0 * 1024.0));
    }
}
