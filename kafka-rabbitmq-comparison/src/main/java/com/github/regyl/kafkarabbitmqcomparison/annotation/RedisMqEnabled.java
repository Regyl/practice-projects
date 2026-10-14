package com.github.regyl.kafkarabbitmqcomparison.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnExpression(value = "'${benchmark.broker}'.equals('all') || '${benchmark.broker}'.equals('redis')")
public @interface RedisMqEnabled {
}
