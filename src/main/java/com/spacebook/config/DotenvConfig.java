package com.spacebook.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads environment variables from .env file in the project root
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    private static final String DOTENV_FILE = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotEnvPath = Paths.get(DOTENV_FILE);
        
        if (Files.exists(dotEnvPath)) {
            Resource resource = new FileSystemResource(dotEnvPath);
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
                // If file can't be read, skip it
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
            
            if (!envMap.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, envMap));
            }
        }
    }
}
