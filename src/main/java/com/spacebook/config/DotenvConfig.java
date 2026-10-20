package com.spacebook.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads environment variables from .env file in the project root
 * and normalizes cloud database connection strings (e.g. Aiven, Render, Railway).
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    private static final String DOTENV_FILE = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final String CLOUD_DB_SOURCE_NAME = "cloudDatabaseConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        loadDotenv(environment);
        normalizeDatabaseConfiguration(environment);
    }

    private void loadDotenv(ConfigurableEnvironment environment) {
        Path dotEnvPath = Paths.get(DOTENV_FILE);
        
        if (Files.exists(dotEnvPath)) {
            Map<String, Object> envMap = new HashMap<>();
            
            try (BufferedReader reader = Files.newBufferedReader(dotEnvPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        envMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
            
            if (!envMap.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, envMap));
            }
        }
    }

    private void normalizeDatabaseConfiguration(ConfigurableEnvironment environment) {
        String rawUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("DATABASE_URL");
        }

        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        Map<String, Object> normalized = parseAndNormalizeDatabaseUrl(rawUrl);
        if (!normalized.isEmpty()) {
            // Keep existing username/password if already explicitly configured
            String existingUser = environment.getProperty("SPRING_DATASOURCE_USERNAME");
            if (existingUser != null && !existingUser.isBlank()) {
                normalized.put("SPRING_DATASOURCE_USERNAME", existingUser);
                normalized.put("spring.datasource.username", existingUser);
            }
            String existingPass = environment.getProperty("SPRING_DATASOURCE_PASSWORD");
            if (existingPass != null && !existingPass.isBlank()) {
                normalized.put("SPRING_DATASOURCE_PASSWORD", existingPass);
                normalized.put("spring.datasource.password", existingPass);
            }

            environment.getPropertySources().addFirst(new MapPropertySource(CLOUD_DB_SOURCE_NAME, normalized));
            System.out.println("SpaceBook: Configured datasource URL: " + normalized.get("spring.datasource.url"));
        }
    }

    /**
     * Parses cloud MySQL URLs (such as Aiven or Railway mysql://user:pass@host:port/db?ssl-mode=REQUIRED)
     * and normalizes them into standard JDBC format with separate credentials.
     */
    public static Map<String, Object> parseAndNormalizeDatabaseUrl(String rawUrl) {
        Map<String, Object> props = new HashMap<>();
        if (rawUrl == null || rawUrl.isBlank()) {
            return props;
        }

        String url = rawUrl.trim();
        boolean startsWithMysql = url.startsWith("mysql://");
        boolean startsWithJdbcMysql = url.startsWith("jdbc:mysql://");

        if (!startsWithMysql && !startsWithJdbcMysql) {
            return props;
        }

        // Strip "jdbc:" if present so java.net.URI can parse the authority
        String uriToParse = url.startsWith("jdbc:") ? url.substring(5) : url;

        try {
            URI uri = new URI(uriToParse);
            String userInfo = uri.getUserInfo();
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();
            String rawQuery = uri.getQuery();

            // Extract credentials from userInfo if present
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    String user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
                    String pass = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
                    props.put("SPRING_DATASOURCE_USERNAME", user);
                    props.put("spring.datasource.username", user);
                    props.put("SPRING_DATASOURCE_PASSWORD", pass);
                    props.put("spring.datasource.password", pass);
                } else {
                    String user = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
                    props.put("SPRING_DATASOURCE_USERNAME", user);
                    props.put("spring.datasource.username", user);
                }
            }

            // Construct standard JDBC MySQL URL
            StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://");
            if (host != null) {
                jdbcUrl.append(host);
            }
            if (port > 0) {
                jdbcUrl.append(":").append(port);
            }
            if (path != null && !path.isBlank()) {
                jdbcUrl.append(path);
            } else {
                jdbcUrl.append("/defaultdb");
            }

            // Convert CLI-style ssl-mode to JDBC sslMode
            if (rawQuery != null && !rawQuery.isBlank()) {
                String normalizedQuery = rawQuery.replace("ssl-mode=", "sslMode=");
                jdbcUrl.append("?").append(normalizedQuery);
            }

            String finalUrl = jdbcUrl.toString();
            props.put("SPRING_DATASOURCE_URL", finalUrl);
            props.put("spring.datasource.url", finalUrl);
        } catch (Exception e) {
            System.err.println("SpaceBook: Could not parse database URL: " + e.getMessage());
        }

        return props;
    }
}
