package com.example.andriuswebapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookCommentsServiceTest {

    @Test
    void returnsCommentsWhenApiRespondsSuccessfully() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                [
                  {
                    "id": 1,
                    "bookIsbn": "9780132350884",
                    "reviewerName": "Andrius",
                    "content": "Useful book",
                    "rating": 5
                  }
                ]
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookCommentsService service = new BookCommentsService(httpClient, new ObjectMapper(), "http://localhost:8081");

        BookCommentsResult result = service.getCommentsForIsbn("9780132350884");

        assertThat(result.available()).isTrue();
        assertThat(result.comments()).containsExactly(new BookCommentView(1L, "Andrius", "Useful book", 5));
    }

    @Test
    void returnsUnavailableWhenApiFails() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("down"));

        BookCommentsService service = new BookCommentsService(httpClient, new ObjectMapper(), "http://localhost:8081");

        BookCommentsResult result = service.getCommentsForIsbn("9780132350884");

        assertThat(result.available()).isFalse();
        assertThat(result.comments()).isEmpty();
    }

    @Test
    void returnsEmptyCommentsWhenApiReturnsNotFound() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(response.body()).thenReturn("""
                {"detail":"Book not found"}
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookCommentsService service = new BookCommentsService(httpClient, new ObjectMapper(), "http://localhost:8081");

        BookCommentsResult result = service.getCommentsForIsbn("9780132350884");

        assertThat(result.available()).isTrue();
        assertThat(result.comments()).isEmpty();
    }

    @Test
    void createsCommentWhenApiAcceptsRequest() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(201);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookCommentsService service = new BookCommentsService(httpClient, new ObjectMapper(), "http://localhost:8081");

        boolean result = service.createCommentForIsbn("9780132350884", "Andrius", "Useful book", 5);

        assertThat(result).isTrue();
    }

    @Test
    void ensuresBookExistsWhenApiAcceptsRequest() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        BookCommentsService service = new BookCommentsService(httpClient, new ObjectMapper(), "http://localhost:8081");

        boolean result = service.ensureBookExists("9780132350884", "Clean Code", "Robert C. Martin", 2008);

        assertThat(result).isTrue();
    }
}
