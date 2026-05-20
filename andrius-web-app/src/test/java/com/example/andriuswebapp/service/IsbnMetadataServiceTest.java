package com.example.andriuswebapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IsbnMetadataServiceTest {

    @Test
    void returnsTitleAuthorAndYearWhenApiRespondsSuccessfully() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                  "ISBN:9780140328721": {
                    "title": "Matilda",
                    "publish_date": "October 1, 1989",
                    "authors": [
                      { "name": "Roald Dahl" }
                    ]
                  }
                }
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        IsbnMetadataService service = new IsbnMetadataService(httpClient, new ObjectMapper(), "https://openlibrary.org");

        Optional<IsbnMetadata> result = service.findByIsbn("9780140328721");

        assertThat(result).contains(new IsbnMetadata("Matilda", "Roald Dahl", 1989));
    }

    @Test
    void returnsEmptyWhenApiHasNoBookForIsbn() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        IsbnMetadataService service = new IsbnMetadataService(httpClient, new ObjectMapper(), "https://openlibrary.org");

        assertThat(service.findByIsbn("9780000000000")).isEmpty();
    }

    @Test
    void returnsEmptyWhenApiFails() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("down"));

        IsbnMetadataService service = new IsbnMetadataService(httpClient, new ObjectMapper(), "https://openlibrary.org");

        assertThat(service.findByIsbn("9780140328721")).isEmpty();
    }
}
