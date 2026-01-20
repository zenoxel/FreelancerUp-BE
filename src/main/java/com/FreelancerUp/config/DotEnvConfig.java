package com.FreelancerUp.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class DotEnvConfig {

    private static final Logger log = LoggerFactory.getLogger(DotEnvConfig.class);

    @PostConstruct
    public void init() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, String> env = dotenv.entries()
                    .stream()
                    .collect(
                            java.util.stream.Collectors.toMap(
                                    DotenvEntry::getKey,
                                    DotenvEntry::getValue
                            )
                    );

            env.forEach((key, value) -> {
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            });

            log.info("✓ Loaded {} environment variables from .env file", env.size());

        } catch (Exception e) {
            log.warn("⚠ Could not load .env file: {}", e.getMessage());
            log.warn("⚠ Using system environment variables instead");
        }
    }
}
