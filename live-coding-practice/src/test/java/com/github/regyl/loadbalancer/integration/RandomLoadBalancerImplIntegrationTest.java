package com.github.regyl.loadbalancer.integration;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.service.strategies.RandomLoadBalancerImpl;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@DefaultUnitTest
class RandomLoadBalancerImplIntegrationTest extends AbstractLoadBalancerIntegrationTest {

    @BeforeEach
    void setup() {
        loadBalancer = new RandomLoadBalancerImpl(MAX_INSTANCES);
    }

    @Test
    @DisplayName("Proves it is real random")
    void test_async() {
        //when
        for (int i = 0; i < MAX_INSTANCES; i++) {
            InstanceModel instance = new InstanceModel("192.0.0." + i);
            loadBalancer.register(instance);
        }

        List<CompletableFuture<InstanceModel>> futures = new ArrayList<>();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1_000_000; i++) {
                CompletableFuture<InstanceModel> future = CompletableFuture.supplyAsync(() -> loadBalancer.get(), executorService);
                futures.add(future);
            }
        }

        Map<String, List<InstanceModel>> res = futures.stream().map(item -> {
            try {
                return item.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(InstanceModel::getAddress));


        Assertions.assertThat(res).hasSize(MAX_INSTANCES);
        Assertions.assertThat(res).allSatisfy((key, val) -> {
            Assertions.assertThat(val).hasSizeGreaterThan(90000);
            Assertions.assertThat(val).hasSizeLessThan(110000);
        });
    }
}
