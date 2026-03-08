package com.example.andriuswebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class AndriusWebAppApplication {

    public static void main(String[] args) {
        configureDatasourceFromRailwayUrl();
        SpringApplication.run(AndriusWebAppApplication.class, args);
    }

    private static void configureDatasourceFromRailwayUrl() {
        if (System.getenv("SPRING_DATASOURCE_URL") != null) {
            return;
        }

        String rawUrl = firstNonBlank(
                System.getenv("DATABASE_PUBLIC_URL"),
                System.getenv("DATABASE_URL")
        );
        if (rawUrl == null) {
            return;
        }

        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            String userInfo = uri.getUserInfo();

            if (host == null || port < 0 || database.isBlank() || userInfo == null || !userInfo.contains(":")) {
                return;
            }

            String[] creds = userInfo.split(":", 2);
            String username = URLDecoder.decode(creds[0], StandardCharsets.UTF_8);
            String password = URLDecoder.decode(creds[1], StandardCharsets.UTF_8);
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";

            System.setProperty("spring.datasource.url", jdbcUrl);
            if (System.getenv("SPRING_DATASOURCE_USERNAME") == null) {
                System.setProperty("spring.datasource.username", username);
            }
            if (System.getenv("SPRING_DATASOURCE_PASSWORD") == null) {
                System.setProperty("spring.datasource.password", password);
            }
        } catch (Exception ignored) {
            // Keep default property resolution if DATABASE_URL cannot be parsed.
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
