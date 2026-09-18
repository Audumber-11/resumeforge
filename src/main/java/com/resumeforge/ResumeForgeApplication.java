package com.resumeforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point.
 *
 * Database selection:
 *  - Default: MySQL via DB_URL / DB_USERNAME / DB_PASSWORD (required for persistence).
 *  - Demo/deploy fallback: set RESUMEFORCE_DB=h2 to run with an in-memory H2 database
 *    (data resets on restart — handy for free tiers without a MySQL add-on).
 */
@SpringBootApplication
public class ResumeForgeApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ResumeForgeApplication.class, args);
    }

    /**
     * Allows RESUMEFORCE_DB=h2 to override datasource settings before the context binds them.
     * Implemented as a customizer so no extra profile files are needed.
     */
    static {
        String db = System.getenv("RESUMEFORCE_DB");
        if ("h2".equalsIgnoreCase(db)) {
            System.setProperty("spring.datasource.url", "jdbc:h2:mem:resumeforge;DB_CLOSE_DELAY=-1;MODE=MySQL");
            System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
            System.setProperty("spring.datasource.username", "sa");
            System.setProperty("spring.datasource.password", "");
            System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        }
    }
}
