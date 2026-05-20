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
public class BookLocationStockService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String stockApiBaseUrl;

    @Autowired
    public BookLocationStockService(@Value("${stock.api.base-url:http://localhost:8082}") String stockApiBaseUrl) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build(), new ObjectMapper(), stockApiBaseUrl);
    }

    BookLocationStockService(HttpClient httpClient, ObjectMapper objectMapper, String stockApiBaseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.stockApiBaseUrl = stockApiBaseUrl;
    }

    public BookLocationStockResult getStockForIsbn(String isbn) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stockApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8) + "/stocks"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                List<ApiLocationStock> stocks = objectMapper.readValue(response.body(), new TypeReference<>() {
                });
                return BookLocationStockResult.available(stocks.stream()
                        .map(stock -> new BookLocationStockView(stock.id(), stock.locationName(), stock.quantity()))
                        .toList());
            }
            if (response.statusCode() == 404) {
                return BookLocationStockResult.available(List.of());
            }
        } catch (IOException exception) {
            return BookLocationStockResult.unavailable();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return BookLocationStockResult.unavailable();
        } catch (IllegalArgumentException exception) {
            return BookLocationStockResult.unavailable();
        }

        return BookLocationStockResult.unavailable();
    }

    public boolean createStockForIsbn(String isbn, String locationName, int quantity) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stockApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8) + "/stocks"))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(jsonBodyPublisher(new CreateLocationStockBody(locationName, quantity)))
                .build();

        return sendMutation(request);
    }

    public boolean updateStock(long stockId, String isbn, String locationName, int quantity) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stockApiBaseUrl + "/api/stocks/" + stockId))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .PUT(jsonBodyPublisher(new UpdateLocationStockBody(isbn, locationName, quantity)))
                .build();

        return sendMutation(request);
    }

    public boolean ensureBookExists(String isbn, String title, String author, int publishedYear) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stockApiBaseUrl + "/api/books/" + URLEncoder.encode(isbn, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .PUT(jsonBodyPublisher(new RegisterBookBody(title, author, publishedYear)))
                .build();

        return sendMutation(request);
    }

    private boolean sendMutation(HttpRequest request) {
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

    private HttpRequest.BodyPublisher jsonBodyPublisher(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not serialize stock request body.", exception);
        }
    }

    private record ApiLocationStock(Long id, String bookIsbn, String locationName, int quantity) {
    }

    private record CreateLocationStockBody(String locationName, int quantity) {
    }

    private record UpdateLocationStockBody(String bookIsbn, String locationName, int quantity) {
    }

    private record RegisterBookBody(String title, String author, int publishedYear) {
    }
}
