package com.github.regyl.volatileatomic;

import com.github.regyl.annotation.DefaultUnitTest;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@DefaultUnitTest
class VolatileAtomicRunnableTest {

    @Test
    @DisplayName("Proves that volatile is not atomic. Result [1 1] describes a situation when changes been lost")
    void test() {
        int iterations = 1_000_000;
        List<Pair<Future<Integer>, Future<Integer>>> results = new ArrayList<>();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < iterations; i++) {
                State state = new State();
                VolatileAtomicRunnable runnable1 = new VolatileAtomicRunnable(state);
                VolatileAtomicRunnable runnable2 = new VolatileAtomicRunnable(state);

                Future<Integer> res1 = executorService.submit(runnable1);
                Future<Integer> res2 = executorService.submit(runnable2);
                results.add(Pair.of(res1, res2));
            }
        }

        results.stream().map(item -> {
            try {
                return Pair.of(item.getKey().get(), item.getValue().get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.groupingBy(item -> String.format("%d %d", item.getKey(), item.getValue())))
                .forEach((key, value) -> {
                    String msg = String.format("Key [%s], val [%d]", key, value.size());
                    System.out.println(msg);
                });
    }
}
