package com.sba.project;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.identity.entity.OwnerManagerId;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.entity.UserRoleId;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** Offline mapping verification against the catalog; does not connect to Supabase. */
class RentalMappingTests {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static JsonNode catalog;
    private static StandardServiceRegistry registry;
    private static Metadata metadata;
    private static List<Class<?>> entities;

    @BeforeAll
    static void buildMetadata() throws Exception {
        catalog = JSON.readTree(Files.readString(Path.of("../docs/database/rental/schema-catalog.json")));
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        entities = new ArrayList<>();
        for (var candidate : scanner.findCandidateComponents("com.sba.project")) {
            entities.add(Class.forName(candidate.getBeanClassName()));
        }
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .applySetting("hibernate.hbm2ddl.auto", "none")
                .build();
        var sources = new MetadataSources(registry);
        entities.forEach(sources::addAnnotatedClass);
        metadata = sources.buildMetadata();
    }

    @AfterAll
    static void closeRegistry() {
        if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
    }

    @Test
    void everyTableAndColumnMatchesCatalog() {
        assertThat(entities).hasSize(34);
        assertThat(metadata.getEntityBindings()).hasSize(34);
        Set<String> actualTables = new HashSet<>();
        int columns = 0;
        for (var binding : metadata.getEntityBindings()) {
            var table = binding.getTable();
            assertThat(table.getSchema()).isEqualTo("rental");
            assertThat(actualTables.add(table.getName())).isTrue();
            Map<String, org.hibernate.mapping.Column> actual = table.getColumns().stream()
                    .collect(Collectors.toMap(org.hibernate.mapping.Column::getName, c -> c));
            List<JsonNode> expected = new ArrayList<>();
            catalog.get("columns").forEach(c -> {
                if (c.get("table_name").asText().equals(table.getName())) expected.add(c);
            });
            assertThat(actual.keySet()).containsExactlyInAnyOrderElementsOf(
                    expected.stream().map(c -> c.get("column_name").asText()).toList());
            for (var c : expected) {
                String name = c.get("column_name").asText();
                var column = actual.get(name);
                String sqlType = c.get("data_type").asText();
                assertThat(column.isNullable()).as(table.getName() + "." + name)
                        .isEqualTo(c.get("nullable").asBoolean());
                if (sqlType.startsWith("character varying")) {
                    assertThat(column.getLength()).isEqualTo(Long.parseLong(sqlType.replaceAll("\\D", "")));
                    assertThat(column.getSqlTypeCode(metadata)).isEqualTo(java.sql.Types.VARCHAR);
                } else if (sqlType.startsWith("numeric")) {
                    var values = sqlType.substring(8, sqlType.length() - 1).split(",");
                    assertThat(column.getPrecision()).isEqualTo(Integer.parseInt(values[0]));
                    assertThat(column.getScale()).isEqualTo(Integer.parseInt(values[1]));
                } else if (Set.of("uuid", "text", "jsonb", "text[]", "date", "bigint", "integer", "boolean", "timestamp with time zone").contains(sqlType)) {
                    String expectedType = sqlType.equals("timestamp with time zone") ? "timestamptz" : sqlType;
                    assertThat(column.getSqlType(metadata)).as(table.getName() + "." + name).isEqualTo(expectedType);
                }
            }
            columns += actual.size();
            String pk = catalog.get("constraints").valueStream()
                    .filter(c -> c.get("table_name").asText().equals(table.getName()) && c.get("type").asText().equals("p"))
                    .findFirst().orElseThrow().get("definition").asText();
            var pkColumns = pk.substring(pk.indexOf('(') + 1, pk.indexOf(')')).split(", ");
            assertThat(table.getPrimaryKey().getColumns().stream().map(org.hibernate.mapping.Column::getName).toList())
                    .containsExactlyInAnyOrder(pkColumns);
        }
        assertThat(columns).isEqualTo(483);
        assertThat(actualTables).containsExactlyInAnyOrderElementsOf(
                catalog.get("tables").valueStream().map(t -> t.get("name").asText()).toList());
    }

    @Test
    void enumsMatchExactDatabaseCheckSets() {
        for (Class<?> entity : entities) {
            String table = entity.getAnnotation(Table.class).name();
            for (Field field : entity.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Enumerated.class)) continue;
                assertThat(field.getAnnotation(Enumerated.class).value()).isEqualTo(EnumType.STRING);
                String column = field.getAnnotation(Column.class).name();
                String check = catalog.get("constraints").valueStream()
                        .filter(c -> c.get("name").asText().equals(table + "_" + column + "_check"))
                        .findFirst().orElseThrow().get("definition").asText();
                var matcher = java.util.regex.Pattern.compile("'([A-Z_]+)'").matcher(check);
                Set<String> values = new HashSet<>();
                while (matcher.find()) values.add(matcher.group(1));
                assertThat(Arrays.stream(field.getType().getEnumConstants()).map(Object::toString).toList())
                        .containsExactlyInAnyOrderElementsOf(values);
            }
        }
    }

    @Test
    void sessionFactoryAndJsonMapperBootstrapWithoutDatabase() {
        try (var factory = (SessionFactoryImplementor) metadata.buildSessionFactory()) {
            assertThat(factory.getMetamodel().getEntities()).hasSize(34);
            var type = factory.getTypeConfiguration().getJavaTypeRegistry().getDescriptor(JsonNode.class);
            try (var session = factory.openSession()) {
                var options = (org.hibernate.engine.spi.SharedSessionContractImplementor) session;
                var mapper = factory.getSessionFactoryOptions().getJsonFormatMapper();
                var value = JSON.readTree("{\"amount\":123,\"items\":[\"test\"]}");
                assertThat(mapper.fromString(mapper.toString(value, type, options), type, options)).isEqualTo(value);
            }
        }
    }

    @Test
    void keysInheritanceAndSensitiveFieldsAreSafe() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        assertThat(new UserRoleId(a, b)).isEqualTo(new UserRoleId(a, b)).hasSameHashCodeAs(new UserRoleId(a, b));
        assertThat(new OwnerManagerId(a, b)).isEqualTo(new OwnerManagerId(a, b));
        assertThat(new OwnerManagerId(a, b)).isNotEqualTo(new OwnerManagerId(b, a));
        assertThat(entities.stream().filter(BaseEntity.class::isAssignableFrom).count()).isEqualTo(9);
        User user = User.builder().passwordHash("password-secret").refreshTokenHash("refresh-secret")
                .verificationSecretHash("challenge-secret").build();
        String serialized = JSON.writeValueAsString(user);
        assertThat(serialized).doesNotContain("password-secret", "refresh-secret", "challenge-secret", "passwordHash", "refreshTokenHash", "verificationSecretHash");
        assertThat(user.toString()).doesNotContain("password-secret", "refresh-secret", "challenge-secret");
    }
}
