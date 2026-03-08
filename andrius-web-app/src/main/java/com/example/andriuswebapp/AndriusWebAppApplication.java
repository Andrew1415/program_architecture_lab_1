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

        if (tryConfigureFromDatabaseUrl(System.getenv("DATABASE_PUBLIC_URL"), true)) {
            return;
        }

        if (tryConfigureFromRailwayProxyVars()) {
            return;
        }

        tryConfigureFromDatabaseUrl(System.getenv("DATABASE_URL"), false);
    }

    private static boolean tryConfigureFromRailwayProxyVars() {
        String host = firstNonBlank(System.getenv("RAILWAY_TCP_PROXY_DOMAIN"));
        String port = firstNonBlank(System.getenv("RAILWAY_TCP_PROXY_PORT"));
        String database = firstNonBlank(System.getenv("POSTGRES_DB"), System.getenv("PGDATABASE"));
        String username = firstNonBlank(System.getenv("POSTGRES_USER"), System.getenv("PGUSER"));
        String password = firstNonBlank(System.getenv("POSTGRES_PASSWORD"), System.getenv("PGPASSWORD"));

        if (host == null || port == null || database == null || username == null || password == null) {
            return false;
        }
        if (isRailwayInternalHost(host)) {
            return false;
        }

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        System.setProperty("spring.datasource.url", jdbcUrl);
        if (System.getenv("SPRING_DATASOURCE_USERNAME") == null) {
            System.setProperty("spring.datasource.username", username);
        }
        if (System.getenv("SPRING_DATASOURCE_PASSWORD") == null) {
            System.setProperty("spring.datasource.password", password);
        }
        return true;
    }

    private static boolean tryConfigureFromDatabaseUrl(String rawUrl, boolean allowInternalHost) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            String userInfo = uri.getUserInfo();

            if (host == null || port < 0 || database.isBlank() || userInfo == null || !userInfo.contains(":")) {
                return false;
            }
            if (!allowInternalHost && isRailwayInternalHost(host)) {
                return false;
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
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isRailwayInternalHost(String host) {
        return host.endsWith(".railway.internal") || host.endsWith(".internal");
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
