package com.example.andriuswebapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

@Service
public class BookCommentsService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String commentsApiBaseUrl;
    private final String commentsApiKey;

    @Autowired
    public BookCommentsService(
            @Value("${comments.api.base-url:http://localhost:8081}") String commentsApiBaseUrl,
            @Value("${comments.api.key:}") String commentsApiKey
    ) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build(), new ObjectMapper(), commentsApiBaseUrl, commentsApiKey);
    }

    BookCommentsService(HttpClient httpClient, ObjectMapper objectMapper, String commentsApiBaseUrl) {
        this(httpClient, objectMapper, commentsApiBaseUrl, "");
    }

    BookCommentsService(HttpClient httpClient, ObjectMapper objectMapper, String commentsApiBaseUrl, String commentsApiKey) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.commentsApiBaseUrl = commentsApiBaseUrl;
        this.commentsApiKey = commentsApiKey;
    }

    public BookCommentsResult getCommentsForIsbn(String isbn) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(commentsApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8) + "/comments"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                List<ApiComment> comments = objectMapper.readValue(response.body(), new TypeReference<>() {
                });
                return BookCommentsResult.available(comments.stream()
                        .map(comment -> new BookCommentView(
                                comment.id(),
                                comment.reviewerName(),
                                comment.content(),
                                comment.rating()
                        ))
                        .toList());
            }
            if (response.statusCode() == 404) {
                return BookCommentsResult.available(List.of());
            }
        } catch (IOException exception) {
            return BookCommentsResult.unavailable();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return BookCommentsResult.unavailable();
        } catch (IllegalArgumentException exception) {
            return BookCommentsResult.unavailable();
        }

        return BookCommentsResult.unavailable();
    }

    public boolean createCommentForIsbn(String isbn, String reviewerName, String content, int rating) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(commentsApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8) + "/comments"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", commentsApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(
                            new ApiCreateCommentBody(reviewerName, content, rating)
                    )))
                    .build();
        } catch (IOException exception) {
            return false;
        }

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean ensureBookExists(String isbn, String title, String author, int publishedYear) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(commentsApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", commentsApiKey)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(
                            new ApiRegisterBookBody(title, author, publishedYear)
                    )))
                    .build();
        } catch (IOException exception) {
            return false;
        }

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private record ApiComment(Long id, String bookIsbn, String reviewerName, String content, int rating) {
    }

    private record ApiCreateCommentBody(String reviewerName, String content, int rating) {
    }

    private record ApiRegisterBookBody(String title, String author, int publishedYear) {
    }
}
