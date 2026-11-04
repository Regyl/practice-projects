package com.github.regyl.util;

import com.github.regyl.exception.ServiceNotFoundException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@UtilityClass
public class ServicePredicateUtil {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Predicate> T getTargetService(Collection<T> services, Object t) {
        List<T> targetServices = services.stream()
                .filter(service -> service.test(t))
                .toList();
        if (targetServices.isEmpty()) {
            log.error("No target service found for {}", t);
            throw new ServiceNotFoundException();
        }

        if (targetServices.size() > 1) {
            log.error("Multiple target services found: {}", targetServices);
            throw new ServiceNotFoundException();
        }

        return targetServices.getFirst();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Predicate> T getTargetService(Collection<T> services, T defaultService, Object t) {
        List<T> targetServices = services.stream()
                .filter(service -> service.test(t))
                .toList();
        if (targetServices.size() > 1) {
            log.error("Multiple target services found: {}", targetServices);
            throw new ServiceNotFoundException();
        }

        if (targetServices.size() == 1) {
            return targetServices.getFirst();
        }
        return defaultService;
    }
}
