package com.example.andriuswebapp.service;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + id));
    }

    public Book createBook(String title, String author, int year) {
        return bookRepository.save(new Book(title, author, year));
    }

    public Book updateBook(Long id, String title, String author, int year) {
        Book book = getBook(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found: " + id);
        }
        bookRepository.deleteById(id);
    }
}
