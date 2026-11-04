package com.github.regyl.loadbalancer.service.strategies;

import com.github.regyl.loadbalancer.exception.DuplicateInstanceException;
import com.github.regyl.loadbalancer.exception.TooManyRegisteredInstances;
import com.github.regyl.loadbalancer.model.LoadBalancerStrategy;
import com.github.regyl.loadbalancer.model.WeightedInstanceModel;
import com.github.regyl.loadbalancer.model.WeightedLoadBalanceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class WeightedRoundRobinLoadBalancerImpl implements LoadBalancer<WeightedInstanceModel> {

    private final int maxInstances;
    private final ReentrantLock lock;
    private final AtomicInteger counter;
    private final List<WeightedLoadBalanceModel> instances;

    public WeightedRoundRobinLoadBalancerImpl(int maxInstances) {
        this.maxInstances = maxInstances;
        this.instances = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.counter = new AtomicInteger(0);
    }

    @Override
    public boolean test(LoadBalancerStrategy loadBalancerStrategy) {
        return LoadBalancerStrategy.WEIGHTED_ROUND_ROBIN == loadBalancerStrategy;
    }

    @Override
    public WeightedInstanceModel get() {
        lock.lock();
        try {
            return get0();
        } finally {
            lock.unlock();
        }
    }

    private WeightedInstanceModel get0() {
        if (instances.isEmpty()) {
            throw new IllegalStateException("IllegalStateException");
        }

        //search for the next alive instance
        for (int i = 0; i < instances.size(); i++) {
            int current = (counter.get() + i) % instances.size();
            WeightedLoadBalanceModel model = instances.get(current);
            if (model.isNotLoaded()) {
                model.decrement();
                counter.set((current + 1) % instances.size());
                return model.getInstance();
            }
        }

        //if none found - reset weights for all
        for (WeightedLoadBalanceModel model : instances) {
            model.resetWeight();
        }

        //then return the first one
        counter.set(0);
        return instances.getFirst().getInstance();
    }

    @Override
    public void register(WeightedInstanceModel instance) {
        if (instance == null) {
            throw new IllegalArgumentException("IllegalArgumentException");
        }

        lock.lock();
        try {
            register0(instance);
        } finally {
            lock.unlock();
        }
    }

    private void register0(WeightedInstanceModel instance) {
        if (instances.size() == maxInstances) {
            throw new TooManyRegisteredInstances("TooManyRegisteredInstances");
        }

        WeightedLoadBalanceModel model = new WeightedLoadBalanceModel(instance);
        if (instances.contains(model)) {
            throw new DuplicateInstanceException("DuplicateInstanceException");
        }

        instances.add(model);
    }

    @Override
    public boolean remove(WeightedInstanceModel instance) {
        lock.lock();
        try {
            WeightedLoadBalanceModel model = new WeightedLoadBalanceModel(instance);
            return instances.remove(model);
        } finally {
            lock.unlock();
        }
    }
}
