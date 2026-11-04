package com.github.regyl.loadbalancer.service.strategies;

import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.model.LoadBalancerStrategy;

import java.util.function.Predicate;

public interface LoadBalancer<T extends InstanceModel> extends Predicate<LoadBalancerStrategy> {

    void register(T instance);

    T get();

    boolean remove(T instance);
}
