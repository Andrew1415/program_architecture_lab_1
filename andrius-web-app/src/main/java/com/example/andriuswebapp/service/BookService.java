package com.example.andriuswebapp.service;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Cacheable("books")
    public List<Book> getAllBooks() {
        return bookRepository.findAllByOrderByIdAsc();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + id));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Book createBook(String title, String author, int year, int stockQuantity) {
        return bookRepository.save(new Book(title, author, year, stockQuantity));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Book updateBook(Long id, String title, String author, int year, int stockQuantity) {
        Book book = getBook(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        book.setStockQuantity(stockQuantity);
        return bookRepository.save(book);
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public boolean purchaseBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found: " + id);
        }
        return bookRepository.decrementStockIfAvailable(id) == 1;
    }
}
