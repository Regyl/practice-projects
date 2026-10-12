package com.github.regyl.kafkarabbitmqcomparison.archunit;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.github.regyl.kafkarabbitmqcomparison.annotation.DefaultArchUnitTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DefaultArchUnitTest
@SuppressWarnings("unused")
class MapperArchTest {

    @ArchTest
    static final ArchRule mappersShouldNotDependOnControllers =
            noClasses()
                    .that().resideInAPackage("..mapper..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..controller.."
                    )
                    .because("Mappers should not be dependent on controllers")
                    .allowEmptyShould(true);
}