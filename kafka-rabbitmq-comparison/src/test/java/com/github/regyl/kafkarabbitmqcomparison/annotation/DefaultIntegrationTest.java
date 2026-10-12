package com.github.regyl.kafkarabbitmqcomparison.annotation;

import org.junit.jupiter.api.Tag;
import com.github.regyl.kafkarabbitmqcomparison.Tags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Tag(Tags.INTEGRATION)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultIntegrationTest {
}