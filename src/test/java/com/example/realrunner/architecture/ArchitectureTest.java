package com.example.realrunner.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.example.realrunner.RealrunnerApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;

@AnalyzeClasses(packagesOf = RealrunnerApplication.class, importOptions = DoNotIncludeTests.class)
class ArchitectureTest {
    private static final String ROOT = "com.example.realrunner";
    private static final List<String> MODULES = List.of("identity", "ingestion", "activity", "analytics", "prediction");

    @ArchTest
    static void onlyPublicApiIsVisibleToOtherModules(JavaClasses classes) {
        for (String module : MODULES) {
            String modulePackage = ROOT + "." + module;
            String[] otherModules = MODULES.stream()
                    .filter(other -> !other.equals(module))
                    .map(other -> ROOT + "." + other + "..")
                    .toArray(String[]::new);

            noClasses()
                    .that()
                    .resideInAnyPackage(otherModules)
                    .should()
                    .dependOnClassesThat(
                            resideInAPackage(modulePackage + "..").and(not(resideInAPackage(modulePackage + ".api.."))))
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAPackage(ROOT + ".*.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(ROOT + ".*.web..", ROOT + ".*.application..", ROOT + ".*.infra..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule webDoesNotDependOnInfrastructure = noClasses()
            .that()
            .resideInAPackage(ROOT + ".*.web..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ROOT + ".*.infra..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule modulesAreFreeOfCycles =
            slices().matching(ROOT + ".(*)..").should().beFreeOfCycles().allowEmptyShould(true);
}
