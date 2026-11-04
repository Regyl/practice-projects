package com.github.regyl.archunit;

import com.github.regyl.annotation.ArchUnitTest;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

//@ArchUnitTest
class ServiceArchTest {

    @ArchTest
    static final ArchRule servicesShouldNotDependOnControllers =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..controller..")
                    .because("Services should not be dependent on controllers");
}
