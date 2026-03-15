package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public BookInventoryResponse getBook(@PathVariable Long id) {
        Book book = bookService.getBook(id);
        return new BookInventoryResponse(book.getId(), book.getTitle(), book.getStockQuantity());
    }

    @PostMapping("/{id}/purchase")
    public PurchaseResponse purchase(@PathVariable Long id) {
        boolean purchased = bookService.purchaseBook(id);
        Book book = bookService.getBook(id);
        return new PurchaseResponse(id, purchased, book.getStockQuantity());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ErrorResponse handleMissingBook(NoSuchElementException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record BookInventoryResponse(Long id, String title, int stockQuantity) {
    }

    public record PurchaseResponse(Long id, boolean purchased, int remainingStock) {
    }

    public record ErrorResponse(String message) {
    }
}
