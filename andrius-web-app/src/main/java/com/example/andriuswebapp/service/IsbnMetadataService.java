package com.example.andriuswebapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IsbnMetadataService {

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String metadataApiBaseUrl;

    @Autowired
    public IsbnMetadataService(@Value("${isbn.metadata.api.base-url:https://openlibrary.org}") String metadataApiBaseUrl) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build(), new ObjectMapper(), metadataApiBaseUrl);
    }

    IsbnMetadataService(HttpClient httpClient, ObjectMapper objectMapper, String metadataApiBaseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.metadataApiBaseUrl = metadataApiBaseUrl;
    }

    public Optional<IsbnMetadata> findByIsbn(String isbn) {
        String normalizedIsbn = isbn == null ? "" : isbn.trim();
        if (normalizedIsbn.isEmpty()) {
            return Optional.empty();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metadataApiBaseUrl
                        + "/api/books?bibkeys=ISBN:"
                        + URLEncoder.encode(normalizedIsbn, StandardCharsets.UTF_8)
                        + "&format=json&jscmd=data"))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode bookNode = root.path("ISBN:" + normalizedIsbn);
            if (bookNode.isMissingNode() || bookNode.isNull()) {
                return Optional.empty();
            }

            String title = bookNode.path("title").asText("").trim();
            String author = extractAuthor(bookNode.path("authors"));
            Integer year = extractYear(bookNode.path("publish_date").asText(""));
            if (title.isEmpty() && author == null && year == null) {
                return Optional.empty();
            }
            return Optional.of(new IsbnMetadata(title, author == null ? "" : author, year));
        } catch (IOException exception) {
            return Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String extractAuthor(JsonNode authorsNode) {
        if (!authorsNode.isArray() || authorsNode.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        Iterator<JsonNode> iterator = authorsNode.elements();
        while (iterator.hasNext()) {
            JsonNode authorNode = iterator.next();
            String name = authorNode.path("name").asText("").trim();
            if (name.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(name);
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    private Integer extractYear(String publishDate) {
        Matcher matcher = YEAR_PATTERN.matcher(publishDate);
        if (!matcher.find()) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }
}
