package com.sba.project;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** Real Boot/JPA bootstrap, with schema validation explicitly disabled ONLY in this offline test. */
@SpringBootTest(classes = {SbaProjectApplication.class, RentalBootstrapTests.OfflineDatabase.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.config.location=classpath:/application.yml,classpath:/application-local.yml",
        "spring.profiles.active=local",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "JWT_SECRET=test-only-secret-that-is-at-least-thirty-two-bytes"
})
class RentalBootstrapTests {
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired DataSource dataSource;
    @Autowired Environment environment;

    @Test
    void contextLoadsAllEntitiesAndLocalYamlWithoutOpeningDatabaseConnection() {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).hasSize(35);
        assertThat(environment.getActiveProfiles()).contains("local");
        assertThat(environment.getProperty("spring.jpa.properties.hibernate.default_schema")).isEqualTo("rental");
        assertThat(environment.getProperty("spring.sql.init.mode")).isEqualTo("never");
        assertThat(environment.getProperty("spring.jpa.open-in-view")).isEqualTo("false");
        verifyNoInteractions(dataSource);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OfflineDatabase {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }
    }
}
