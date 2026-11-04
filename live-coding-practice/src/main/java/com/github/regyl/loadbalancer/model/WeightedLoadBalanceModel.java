package com.github.regyl.loadbalancer.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class WeightedLoadBalanceModel {

    @Getter
    private final WeightedInstanceModel instance;

    private final AtomicInteger currentWeight;

    public WeightedLoadBalanceModel(WeightedInstanceModel instance) {
        this.instance = instance;
        this.currentWeight = new AtomicInteger(instance.getWeight());
    }

    public void resetWeight() {
        this.currentWeight.set(instance.getWeight());
    }

    public boolean isNotLoaded() {
        return this.currentWeight.get() > 0;
    }

    public int decrement() {
        return this.currentWeight.decrementAndGet();
    }
}
