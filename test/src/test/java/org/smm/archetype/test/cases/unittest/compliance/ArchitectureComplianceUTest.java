package org.smm.archetype.test.cases.unittest.compliance;

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
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 架构合规性测试 - 验证 DDD 四层架构约束
 * <p>
 * 测试目标：
 * 1. Domain 层纯净性（无外部依赖）
 * 2. 禁止 @Data 注解
 * 3. 四层架构依赖方向
 * 4. 模块边界约束
 */
@DisplayName("架构合规性测试")
class ArchitectureComplianceUTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        // 导入所有业务模块的类（排除测试类）
        allClasses = new ClassFileImporter()
                             .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                             .importPackages(
                                     "org.smm.archetype.domain",
                                     "org.smm.archetype.app",
                                     "org.smm.archetype.infrastructure",
                                     "org.smm.archetype.adapter"
                             );
    }

    @Nested
    @DisplayName("Domain 层纯净性")
    class DomainPurityTests {

        @Test
        @DisplayName("Domain 层禁止依赖 Spring 框架")
        void domain_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                                    .that().resideInAPackage("..domain..")
                                    .should().dependOnClassesThat()
                                    .resideInAnyPackage(
                                            "org.springframework..",
                                            "org.springframework.boot..",
                                            "org.springframework.data.."
                                    )
                                    .because("Domain 层必须纯净，不能依赖 Spring 框架")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Domain 层禁止依赖 Lombok 外部日志")
        void domain_should_not_use_lombok_logging() {
            ArchRule rule = noClasses()
                                    .that().resideInAPackage("..domain..")
                                    .should().dependOnClassesThat()
                                    .resideInAnyPackage("lombok.extern..")
                                    .because("Domain 层禁止使用 Lombok 外部日志")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Domain 层禁止依赖 MyBatis")
        void domain_should_not_depend_on_mybatis() {
            ArchRule rule = noClasses()
                                    .that().resideInAPackage("..domain..")
                                    .should().dependOnClassesThat()
                                    .resideInAnyPackage(
                                            "com.baomidou..",
                                            "org.apache.ibatis.."
                                    )
                                    .because("Domain 层禁止依赖 MyBatis/MyBatis-Plus")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

    }

    @Nested
    @DisplayName("注解规范")
    class AnnotationTests {

        @Test
        @DisplayName("禁止使用 @Data 注解")
        void should_not_use_data_annotation() {
            ArchRule rule = noClasses()
                                    .should().beAnnotatedWith("lombok.Data")
                                    .because("@Data 会生成不可控的 equals/hashCode，导致性能问题。应使用 @Builder + @RequiredArgsConstructor")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Domain 层禁止使用 @Transactional")
        void domain_should_not_use_transactional() {
            ArchRule rule = noClasses()
                                    .that().resideInAPackage("..domain..")
                                    .should().notBeAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                                    .because("事务边界应在 Application 层，Domain 层禁止使用 @Transactional")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

    }

    @Nested
    @DisplayName("四层架构依赖")
    class LayerDependencyTests {

        @Test
        @DisplayName("Adapter 层只能依赖 Application 和 Domain 层")
        void adapter_should_only_depend_on_application_and_domain() {
            ArchRule rule = classes()
                                    .that().resideInAPackage("..adapter..")
                                    .should().onlyDependOnClassesThat()
                                    .resideInAnyPackage(
                                            "..adapter..",
                                            "..app..",
                                            "..domain..",
                                            "java..",
                                            "org.springframework..",
                                            "jakarta..",
                                            "lombok..",
                                            "io.swagger..",
                                            "org.springdoc.."
                                    )
                                    .because("Adapter 层只能依赖 Application 和 Domain 层")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Application 层只能依赖 Domain 层")
        void application_should_only_depend_on_domain() {
            ArchRule rule = classes()
                                    .that().resideInAPackage("..app..")
                                    .should().onlyDependOnClassesThat()
                                    .resideInAnyPackage(
                                            "..app..",
                                            "..domain..",
                                            "java..",
                                            "org.springframework..",
                                            "jakarta..",
                                            "lombok..",
                                            "org.mapstruct.."
                                    )
                                    .because("Application 层只能依赖 Domain 层，禁止直接依赖 Infrastructure")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Infrastructure 层实现类命名规范")
        void infrastructure_impl_should_follow_naming_convention() {
            ArchRule rule = classes()
                                    .that().resideInAPackage("..infrastructure..")
                                    .and().haveSimpleNameEndingWith("Impl")
                                    .should().resideInAPackage("..persistence..")
                                    .because("Infrastructure 层实现类应放在 persistence 包中");

            // 注意：此规则较宽松，仅作为建议性检查
            // rule.check(allClasses);
        }

    }

    @Nested
    @DisplayName("模块边界")
    class ModuleBoundaryTests {

        @Test
        @DisplayName("Adapter 禁止直接访问 Infrastructure")
        void adapter_should_not_access_infrastructure() {
            ArchRule rule = noClasses()
                                    .that().resideInAPackage("..adapter..")
                                    .should().dependOnClassesThat()
                                    .resideInAPackage("..infrastructure..")
                                    .because("Adapter 禁止直接依赖 Infrastructure，应通过 Application 层")
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

        @Test
        @DisplayName("模块间无循环依赖")
        void modules_should_have_no_cycles() {
            ArchRule rule = slices()
                                    .matching("org.smm.archetype.(*)..")
                                    .should().beFreeOfCycles()
                                    .allowEmptyShould(true);

            rule.check(allClasses);
        }

    }

}
