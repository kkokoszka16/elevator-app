package com.elevator.infrastructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Hexagonal Architecture Rules")
class ArchitectureTest {

    private static final String DOMAIN_PACKAGE = "com.elevator.domain..";
    private static final String APPLICATION_PACKAGE = "com.elevator.application..";
    private static final String INFRASTRUCTURE_PACKAGE = "com.elevator.infrastructure..";
    private static final String ADAPTER_PACKAGE = "com.elevator.infrastructure.adapter..";
    private static final String PORT_PACKAGE = "com.elevator.application.port..";

    private static final String SPRING_PACKAGE = "org.springframework..";
    private static final String JAKARTA_PACKAGE = "jakarta..";
    private static final String LOMBOK_PACKAGE = "lombok..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.elevator");
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("domain_should_not_depend_on_spring")
        void domain_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(SPRING_PACKAGE)
                    .because("Domain layer must be framework-free (hexagonal architecture principle)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain_should_not_depend_on_jakarta")
        void domain_should_not_depend_on_jakarta() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(JAKARTA_PACKAGE)
                    .because("Domain layer must be framework-free (hexagonal architecture principle)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain_should_not_depend_on_adapters")
        void domain_should_not_depend_on_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER_PACKAGE)
                    .because("Domain cannot depend on infrastructure adapters (dependency inversion)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain_should_not_depend_on_infrastructure")
        void domain_should_not_depend_on_infrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain cannot depend on infrastructure layer");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain_should_not_depend_on_application")
        void domain_should_not_depend_on_application() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain is the innermost layer and cannot depend on application layer");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerRules {

        @Test
        @DisplayName("application_should_not_depend_on_adapters")
        void application_should_not_depend_on_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER_PACKAGE)
                    .because("Application layer cannot depend on infrastructure adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("application_should_not_depend_on_infrastructure")
        void application_should_not_depend_on_infrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application layer cannot depend on infrastructure layer");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("application_should_not_depend_on_spring")
        void application_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(SPRING_PACKAGE)
                    .because("Application layer must be framework-free (hexagonal architecture principle)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("application_should_only_depend_on_domain")
        void application_should_only_depend_on_domain() {
            ArchRule rule = classes()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            APPLICATION_PACKAGE,
                            DOMAIN_PACKAGE,
                            "java..",
                            LOMBOK_PACKAGE,
                            "org.slf4j.."
                    )
                    .because("Application layer can only depend on domain and standard libraries");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Adapter Independence Rules")
    class AdapterIndependenceRules {

        private static final String WEB_ADAPTER_PACKAGE = "com.elevator.infrastructure.adapter.web..";
        private static final String PERSISTENCE_ADAPTER_PACKAGE = "com.elevator.infrastructure.adapter.persistence..";
        private static final String EVENT_ADAPTER_PACKAGE = "com.elevator.infrastructure.adapter.event..";
        private static final String DTO_PACKAGE = "com.elevator.infrastructure.adapter.web.dto..";

        @Test
        @DisplayName("web_adapters_should_not_depend_on_persistence_adapters")
        void web_adapters_should_not_depend_on_persistence_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(WEB_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(PERSISTENCE_ADAPTER_PACKAGE)
                    .because("Web adapters should be independent of persistence adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("web_adapters_should_not_depend_on_event_adapters")
        void web_adapters_should_not_depend_on_event_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(WEB_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(EVENT_ADAPTER_PACKAGE)
                    .because("Web adapters should be independent of event adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("persistence_adapters_should_not_depend_on_web_adapters")
        void persistence_adapters_should_not_depend_on_web_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(PERSISTENCE_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(WEB_ADAPTER_PACKAGE)
                    .because("Persistence adapters should be independent of web adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("persistence_adapters_should_not_depend_on_event_adapters")
        void persistence_adapters_should_not_depend_on_event_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(PERSISTENCE_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(EVENT_ADAPTER_PACKAGE)
                    .because("Persistence adapters should be independent of event adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("event_adapters_should_not_depend_on_web_controllers")
        void event_adapters_should_not_depend_on_web_controllers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(EVENT_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat()
                    .haveSimpleNameEndingWith("Controller")
                    .because("Event adapters should not depend on web controllers");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("event_adapters_should_not_depend_on_persistence_adapters")
        void event_adapters_should_not_depend_on_persistence_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(EVENT_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(PERSISTENCE_ADAPTER_PACKAGE)
                    .because("Event adapters should be independent of persistence adapters");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("adapters_should_be_independent")
        void adapters_should_be_independent() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(PERSISTENCE_ADAPTER_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(WEB_ADAPTER_PACKAGE, EVENT_ADAPTER_PACKAGE)
                    .because("Persistence adapters should not depend on web or event adapters");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Port Rules")
    class PortRules {

        @Test
        @DisplayName("ports_should_be_interfaces")
        void ports_should_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(PORT_PACKAGE)
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces()
                    .because("Input ports (use cases) should be interfaces");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("output_ports_should_be_interfaces")
        void output_ports_should_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage("com.elevator.application.port.out..")
                    .should().beInterfaces()
                    .because("Output ports should be interfaces for dependency inversion");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("ports_should_not_depend_on_spring")
        void ports_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(PORT_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(SPRING_PACKAGE)
                    .because("Ports are part of application layer and must be framework-free");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Dependency Direction Rules")
    class DependencyDirectionRules {

        @Test
        @DisplayName("infrastructure_may_depend_on_application")
        void infrastructure_may_depend_on_application() {
            ArchRule rule = classes()
                    .that().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            INFRASTRUCTURE_PACKAGE,
                            APPLICATION_PACKAGE,
                            DOMAIN_PACKAGE,
                            SPRING_PACKAGE,
                            JAKARTA_PACKAGE,
                            LOMBOK_PACKAGE,
                            "java..",
                            "org.slf4j..",
                            "org.springdoc..",
                            "io.swagger..",
                            "io.micrometer..",
                            "io.github.bucket4j..",
                            "org.mapstruct..",
                            "com.fasterxml.."
                    )
                    .because("Infrastructure layer depends on inner layers (application and domain)");

            rule.check(importedClasses);
        }
    }
}
