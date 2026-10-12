/*
package com.github.regyl.kafkarabbitmqcomparison.archunit;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import ru.sbrf.ssoar.ssoarcommonlib.exception.NonRetryableException;
import ru.sbrf.ssoar.ssoarcommonlib.exception.RetryableException;
import com.github.regyl.kafkarabbitmqcomparison.annotation.DefaultArchUnitTest;

@DefaultArchUnitTest
@SuppressWarnings("unused")
class ExceptionArchTest {

    @ArchTest
    static final ArchRule instanceOfAnnotationTest =
            ArchRuleDefinition.classes()
                    .that().resideInAPackage("..exception..").and().resideOutsideOfPackage("..configuration.exception..")
                    .should().beAssignableTo(RetryableException.class).orShould().beAssignableTo(NonRetryableException.class)
                    .because("All util classes should have private constructors")
                    .allowEmptyShould(true);
}
*/
