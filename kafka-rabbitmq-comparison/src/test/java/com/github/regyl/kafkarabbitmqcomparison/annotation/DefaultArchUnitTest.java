package com.github.regyl.kafkarabbitmqcomparison.annotation;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import org.junit.jupiter.api.Tag;
import com.github.regyl.kafkarabbitmqcomparison.Tags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Tag(Tags.UNIT)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnalyzeClasses(
        packages = "com.github.regyl",
        importOptions = { ImportOption.DoNotIncludeTests.class }
)
public @interface DefaultArchUnitTest {
}