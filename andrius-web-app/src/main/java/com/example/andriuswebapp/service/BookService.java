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

    public boolean isbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    public boolean isbnExistsForAnotherBook(String isbn, Long id) {
        return bookRepository.existsByIsbnAndIdNot(isbn, id);
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Book createBook(String isbn, String title, String author, int year) {
        return bookRepository.save(new Book(isbn, title, author, year, 0));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Book updateBook(Long id, String isbn, String title, String author, int year) {
        Book book = getBook(id);
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
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
