package com.github.regyl.loadbalancer.service.strategies;

import com.github.regyl.loadbalancer.exception.DuplicateInstanceException;
import com.github.regyl.loadbalancer.exception.TooManyRegisteredInstances;
import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.model.LoadBalancerStrategy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RandomLoadBalancerImpl implements LoadBalancer<InstanceModel> {

    private final Random random;
    private final ReadWriteLock lock;
    private final List<InstanceModel> instances;
    private final int maxInstances;

    public RandomLoadBalancerImpl(int maxInstances) {
        this.instances = new ArrayList<>();
        this.maxInstances = maxInstances;
        this.random = new SecureRandom();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean test(LoadBalancerStrategy loadBalancerStrategy) {
        return LoadBalancerStrategy.RANDOM == loadBalancerStrategy;
    }

    @Override
    public InstanceModel get() {
        lock.readLock().lock();
        try {
            if (instances.isEmpty()) {
                throw new IllegalStateException("IllegalStateException");
            }

            int val = random.nextInt(0, instances.size());
            return instances.get(val);
        } finally {
            lock.readLock().unlock();
        }
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
