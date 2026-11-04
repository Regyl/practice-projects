package com.github.regyl.loadbalancer.integration;

import com.github.regyl.loadbalancer.exception.DuplicateInstanceException;
import com.github.regyl.loadbalancer.exception.TooManyRegisteredInstances;
import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.service.strategies.LoadBalancer;
import com.github.regyl.loadbalancer.service.strategies.RoundRobinLoadBalancerImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

abstract class AbstractLoadBalancerIntegrationTest {

    protected static final int MAX_INSTANCES = 10;

    protected LoadBalancer<InstanceModel> loadBalancer;

    @Test
    void test_register_get() {
        //given
        InstanceModel instance = new InstanceModel("192.0.0.1");
        loadBalancer.register(instance);

        //when
        InstanceModel result = loadBalancer.get();

        //then
        Assertions.assertThat(result).isEqualTo(instance);
    }

    @Test
    void should_register_no_more_than_max_instances() {
        //when
        for (int i = 0; i < MAX_INSTANCES; i++) {
            InstanceModel instance = new InstanceModel("192.0.0." + i);
            loadBalancer.register(instance);
        }
        InstanceModel backendInstance = new InstanceModel("192.0.0.100");

        //then
        Assertions.assertThatCode(() -> loadBalancer.register(backendInstance))
                .isInstanceOf(TooManyRegisteredInstances.class)
                .hasMessage("TooManyRegisteredInstances");
    }

    @Test
    void should_throw_duplicate_exception_on_same_input() {
        //given
        InstanceModel instance = new InstanceModel("192.0.0.1");
        loadBalancer.register(instance);
        InstanceModel instance1 = new InstanceModel("192.0.0.1");

        //then
        Assertions.assertThatCode(() -> loadBalancer.register(instance1))
                .isInstanceOf(DuplicateInstanceException.class)
                .hasMessage("DuplicateInstanceException");
    }

    @Test
    void should_throw_illegal_argument_on_null_input() {
        Assertions.assertThatCode(() -> loadBalancer.register(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IllegalArgumentException");
    }

    @Test
    void remove_should_remove_instance() {
        InstanceModel instance = new InstanceModel("192.0.0.1");
        loadBalancer.register(instance);

        InstanceModel instance1 = new InstanceModel("192.0.0.1");
        loadBalancer.remove(instance1);

        Assertions.assertThatCode(() -> loadBalancer.get())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("IllegalStateException");
    }

    @Test
    void concurrent_updates_should_be_readable_from_different_threads() {
        LoadBalancer balancer = new RoundRobinLoadBalancerImpl(Integer.MAX_VALUE);
        int iterations = 10000;

        List<InstanceModel> instances = IntStream.range(0, iterations)
                .mapToObj(i -> new InstanceModel("192.0.0." + i))
                .toList();

        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < iterations; i++) {
                InstanceModel instance = instances.get(i);
                service.submit(() -> balancer.register(instance));
            }
        }

        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < iterations; i++) {
                InstanceModel instance = instances.get(i);
                service.submit(() -> balancer.remove(instance));
            }
        }

        Assertions.assertThatCode(() -> loadBalancer.get())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("IllegalStateException");
    }
}
