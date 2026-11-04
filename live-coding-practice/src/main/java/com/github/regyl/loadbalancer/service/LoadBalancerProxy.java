package com.github.regyl.loadbalancer.service;

import com.github.regyl.loadbalancer.model.InstanceModel;
import com.github.regyl.loadbalancer.model.LoadBalancerStrategy;
import com.github.regyl.loadbalancer.service.strategies.LoadBalancer;
import com.github.regyl.util.ServicePredicateUtil;

import java.util.Collection;

/**
 * Looks like there is no place for strategy pattern.
 * Or just implement this proxy like container for a specific {@link LoadBalancer}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class LoadBalancerProxy {

    private final Collection<LoadBalancer> loadBalancers;

    public LoadBalancerProxy(Collection<LoadBalancer> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    public String get(LoadBalancerStrategy strategy) {
        LoadBalancer balancer = ServicePredicateUtil.getTargetService(loadBalancers, strategy);
        return balancer.get().getAddress();
    }

    public <T extends InstanceModel> void register(T instance, LoadBalancerStrategy strategy) {
        LoadBalancer balancer = ServicePredicateUtil.getTargetService(loadBalancers, strategy);
        balancer.register(instance);
    }

    public <T extends InstanceModel> boolean remove(T instance, LoadBalancerStrategy strategy) {
        LoadBalancer balancer = ServicePredicateUtil.getTargetService(loadBalancers, strategy);
        return balancer.remove(instance);
    }
}
