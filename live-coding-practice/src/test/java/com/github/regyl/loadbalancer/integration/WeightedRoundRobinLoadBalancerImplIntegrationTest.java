package com.github.regyl.loadbalancer.integration;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.loadbalancer.model.WeightedInstanceModel;
import com.github.regyl.loadbalancer.service.strategies.LoadBalancer;
import com.github.regyl.loadbalancer.service.strategies.WeightedRoundRobinLoadBalancerImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@DefaultUnitTest
class WeightedRoundRobinLoadBalancerImplIntegrationTest {

    private static final int MAX_INSTANCES = 10;

    private LoadBalancer<WeightedInstanceModel> loadBalancer;

    @BeforeEach
    void setup() {
        loadBalancer = new WeightedRoundRobinLoadBalancerImpl(MAX_INSTANCES);
    }

    @Test
    void check_weighted_round_robin() {
        WeightedInstanceModel instance1 = new WeightedInstanceModel("192.0.0.1", 1);
        WeightedInstanceModel instance2 = new WeightedInstanceModel("192.0.0.2", 2);
        loadBalancer.register(instance1);
        loadBalancer.register(instance2);

        WeightedInstanceModel model1 = loadBalancer.get();
        WeightedInstanceModel model2 = loadBalancer.get();
        WeightedInstanceModel model3 = loadBalancer.get();
        WeightedInstanceModel model4 = loadBalancer.get();

        Assertions.assertThat(model1).isEqualTo(instance1);
        Assertions.assertThat(model2).isEqualTo(instance2);
        Assertions.assertThat(model3).isEqualTo(instance2);
        Assertions.assertThat(model4).isEqualTo(instance1);
    }

    @Test
    @DisplayName("Proves it is real weight round-robin")
    void test_async() {
        //when
        for (int i = 0; i < MAX_INSTANCES; i++) {
            WeightedInstanceModel instance = new WeightedInstanceModel("192.0.0." + i, i + 1);
            loadBalancer.register(instance);
        }

        List<CompletableFuture<WeightedInstanceModel>> futures = new ArrayList<>();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1_000_000; i++) {
                CompletableFuture<WeightedInstanceModel> future = CompletableFuture.supplyAsync(() -> loadBalancer.get(), executorService);
                futures.add(future);
            }
        }

        Map<String, List<WeightedInstanceModel>> res = futures.stream().map(item -> {
            try {
                return item.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(WeightedInstanceModel::getAddress));
        List<Pair<String, Integer>> counted = res.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().size()))
                .toList();
        counted = new ArrayList<>(counted);
        counted.sort(Comparator.comparingInt(Pair::getValue));

        Assertions.assertThat(res).hasSize(MAX_INSTANCES);
        System.out.println(counted);
    }
}
