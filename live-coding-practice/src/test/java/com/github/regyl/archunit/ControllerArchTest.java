package com.github.regyl.archunit;

import com.github.regyl.annotation.ArchUnitTest;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

//@ArchUnitTest
class ControllerArchTest {

    @ArchTest
    static final ArchRule controllersShouldNotDependOnRepositories =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..repository..")
                    .because("Controllers should not be dependent on repositories");
}
