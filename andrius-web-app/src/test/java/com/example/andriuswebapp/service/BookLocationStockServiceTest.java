package com.example.andriuswebapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookLocationStockServiceTest {

    @Test
    void returnsStocksWhenApiRespondsSuccessfully() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                [
                  {
                    "id": 1,
                    "bookIsbn": "9780132350884",
                    "locationName": "Vilnius Central",
                    "quantity": 4
                  }
                ]
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        BookLocationStockResult result = service.getStockForIsbn("9780132350884");

        assertThat(result.available()).isTrue();
        assertThat(result.items()).containsExactly(new BookLocationStockView(1L, "Vilnius Central", 4));
    }

    @Test
    void returnsEmptyWhenApiReturnsNotFound() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(response.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        BookLocationStockResult result = service.getStockForIsbn("9780132350884");

        assertThat(result.available()).isTrue();
        assertThat(result.items()).isEmpty();
    }

    @Test
    void returnsUnavailableWhenApiFails() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("down"));

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        BookLocationStockResult result = service.getStockForIsbn("9780132350884");

        assertThat(result.available()).isFalse();
        assertThat(result.items()).isEmpty();
    }

    @Test
    void createsStockUsingBookSpecificEndpoint() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(201);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        boolean created = service.createStockForIsbn("9780132350884", "Vilnius Central", 4);

        assertThat(created).isTrue();
        org.mockito.ArgumentCaptor<HttpRequest> requestCaptor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        org.mockito.Mockito.verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requestCaptor.getValue().uri()).isEqualTo(URI.create("http://localhost:8082/api/books/9780132350884/stocks"));
        assertThat(requestCaptor.getValue().method()).isEqualTo("POST");
    }

    @Test
    void updatesStockUsingStockSpecificEndpoint() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        boolean updated = service.updateStock(7L, "9780132350884", "Kaunas Branch", 9);

        assertThat(updated).isTrue();
        org.mockito.ArgumentCaptor<HttpRequest> requestCaptor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        org.mockito.Mockito.verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requestCaptor.getValue().uri()).isEqualTo(URI.create("http://localhost:8082/api/stocks/7"));
        assertThat(requestCaptor.getValue().method()).isEqualTo("PUT");
    }

    @Test
    void ensuresBookExistsUsingBookRegistrationEndpoint() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookLocationStockService service = new BookLocationStockService(httpClient, new ObjectMapper(), "http://localhost:8082");

        boolean ensured = service.ensureBookExists("9780132350884", "Clean Code", "Robert C. Martin", 2008);

        assertThat(ensured).isTrue();
        org.mockito.ArgumentCaptor<HttpRequest> requestCaptor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        org.mockito.Mockito.verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requestCaptor.getValue().uri()).isEqualTo(URI.create("http://localhost:8082/api/books/9780132350884"));
        assertThat(requestCaptor.getValue().method()).isEqualTo("PUT");
    }
}
