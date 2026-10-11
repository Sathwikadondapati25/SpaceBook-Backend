package com.spacebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SpaceBook Application
 * Conflict-free event stall booking platform
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SpaceBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpaceBookApplication.class, args);
    }
}
