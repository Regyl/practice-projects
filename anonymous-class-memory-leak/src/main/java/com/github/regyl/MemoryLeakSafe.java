package com.github.regyl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryLeakSafe {
    //simulate a long-living application
    static List<Set<String>> cache = new ArrayList<>();

    static class Service {
        byte[] data = new byte[100 * 1024 * 1024]; // 100 MB

        Set<String> countries = new HashSet<>();

        public Service() {
            countries.add("USA");
        }
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

        MemoryUtil.printUsed(); //2.5 MB
    }
}
