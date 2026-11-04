package com.github.regyl.loadbalancer.service.strategies;

import com.github.regyl.loadbalancer.exception.DuplicateInstanceException;
import com.github.regyl.loadbalancer.exception.TooManyRegisteredInstances;
import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.model.LoadBalancerStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoundRobinLoadBalancerImpl implements LoadBalancer<InstanceModel> {

    private final ReadWriteLock lock;
    private final AtomicInteger counter;
    private final List<InstanceModel> instances;
    private final int maxInstances;

    public RoundRobinLoadBalancerImpl(int maxInstances) {
        this.instances = new ArrayList<>();
        this.maxInstances = maxInstances;
        this.lock = new ReentrantReadWriteLock();
        this.counter = new AtomicInteger(0);
    }

    @Override
    public boolean test(LoadBalancerStrategy loadBalancerStrategy) {
        return LoadBalancerStrategy.ROUND_ROBIN == loadBalancerStrategy;
    }

    @Override
    public InstanceModel get() {
        lock.readLock().lock();
        try {
            return get0();
        } finally {
            lock.readLock().unlock();
        }
    }

    private InstanceModel get0() {
        if (instances.isEmpty()) {
            throw new IllegalStateException("IllegalStateException");
        }

        int val = counter.getAndUpdate(current -> (current + 1) % instances.size());
        return instances.get(val);
    }

    @Override
    public void register(InstanceModel instance) {
        if (instance == null) {
            throw new IllegalArgumentException("IllegalArgumentException");
        }

        lock.writeLock().lock();
        try {
            register0(instance);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void register0(InstanceModel instance) {
        if (instances.size() == maxInstances) {
            throw new TooManyRegisteredInstances("TooManyRegisteredInstances");
        }
        if (instances.contains(instance)) {
            throw new DuplicateInstanceException("DuplicateInstanceException");
        }

        instances.add(instance);
    }

    @Override
    public boolean remove(InstanceModel instance) {
        lock.writeLock().lock();
        try {
            return instances.remove(instance);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
