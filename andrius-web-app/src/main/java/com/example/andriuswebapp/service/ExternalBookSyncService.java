package com.example.andriuswebapp.service;

import com.example.andriuswebapp.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalBookSyncService {

    private final BookCommentsService bookCommentsService;
    private final BookLocationStockService bookLocationStockService;
    private final boolean enabled;

    public ExternalBookSyncService(BookCommentsService bookCommentsService,
                                   BookLocationStockService bookLocationStockService,
                                   @Value("${external.book-sync.enabled:true}") boolean enabled) {
        this.bookCommentsService = bookCommentsService;
        this.bookLocationStockService = bookLocationStockService;
        this.enabled = enabled;
    }

    public void syncBook(Book book) {
        if (!enabled) {
            return;
        }
        bookCommentsService.ensureBookExists(book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear());
        bookLocationStockService.ensureBookExists(book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear());
    }

    public boolean ensureCommentsBookExists(Book book) {
        return !enabled || bookCommentsService.ensureBookExists(
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear()
        );
    }

    public boolean ensureStockBookExists(Book book) {
        return !enabled || bookLocationStockService.ensureBookExists(
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear()
        );
    }
}
