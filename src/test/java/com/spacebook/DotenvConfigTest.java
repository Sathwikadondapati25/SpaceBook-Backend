package com.spacebook;

import com.spacebook.config.DotenvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DotenvConfigTest {

    @Test
    @DisplayName("Should normalize cloud mysql:// URL with credentials and query parameters")
    void testNormalizeCloudMysqlUrl() {
        String cloudUrl = "mysql://clouduser:sampleSecretPass123@mysql-sample-host.aivencloud.com:25323/defaultdb?ssl-mode=REQUIRED";

        Map<String, Object> props = DotenvConfig.parseAndNormalizeDatabaseUrl(cloudUrl);

        assertFalse(props.isEmpty());
        assertEquals("jdbc:mysql://mysql-sample-host.aivencloud.com:25323/defaultdb?sslMode=REQUIRED", props.get("SPRING_DATASOURCE_URL"));
        assertEquals("jdbc:mysql://mysql-sample-host.aivencloud.com:25323/defaultdb?sslMode=REQUIRED", props.get("spring.datasource.url"));
        assertEquals("clouduser", props.get("SPRING_DATASOURCE_USERNAME"));
        assertEquals("clouduser", props.get("spring.datasource.username"));
        assertEquals("sampleSecretPass123", props.get("SPRING_DATASOURCE_PASSWORD"));
        assertEquals("sampleSecretPass123", props.get("spring.datasource.password"));
    }

    @Test
    @DisplayName("Should normalize jdbc:mysql:// URL with embedded credentials")
    void testNormalizeJdbcMysqlWithCredentials() {
        String url = "jdbc:mysql://myuser:mypass@localhost:3306/spacebook";

        Map<String, Object> props = DotenvConfig.parseAndNormalizeDatabaseUrl(url);

        assertFalse(props.isEmpty());
        assertEquals("jdbc:mysql://localhost:3306/spacebook", props.get("SPRING_DATASOURCE_URL"));
        assertEquals("myuser", props.get("SPRING_DATASOURCE_USERNAME"));
        assertEquals("mypass", props.get("SPRING_DATASOURCE_PASSWORD"));
    }

    @Test
    @DisplayName("Should leave standard JDBC URL intact")
    void testStandardJdbcUrl() {
        String url = "jdbc:mysql://localhost:3306/spacebook?useSSL=false&serverTimezone=UTC";

        Map<String, Object> props = DotenvConfig.parseAndNormalizeDatabaseUrl(url);

        assertFalse(props.isEmpty());
        assertEquals(url, props.get("SPRING_DATASOURCE_URL"));
        assertNull(props.get("SPRING_DATASOURCE_USERNAME"));
        assertNull(props.get("SPRING_DATASOURCE_PASSWORD"));
    }
}
