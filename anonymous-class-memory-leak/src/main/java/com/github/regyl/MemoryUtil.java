package com.github.regyl;

public class MemoryUtil {

    public static long getUsed() {
        Runtime runtime = Runtime.getRuntime();

        // 2. Total Allocated Heap Size (Current memory pool reserved from OS)
        long totalMemory = runtime.totalMemory();

        // 3. Free Memory inside the currently allocated pool
        long freeMemory = runtime.freeMemory();

        // 4. Actually used memory (Total allocated minus free space)
        return totalMemory - freeMemory;
    }

    public static void printUsed() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("Approx. used heap: %.2f MB%n",
                used / (1024.0 * 1024.0));
    }
}
