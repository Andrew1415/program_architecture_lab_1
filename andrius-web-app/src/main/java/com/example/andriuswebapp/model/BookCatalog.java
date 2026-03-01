package com.example.andriuswebapp.model;

import java.util.List;

public final class BookCatalog {

    private static final List<Book> BOOKS = List.of(
            new Book(1L, "Clean Code", "Robert C. Martin", 2008),
            new Book(2L, "Refactoring", "Martin Fowler", 2018),
            new Book(3L, "Domain-Driven Design", "Eric Evans", 2003),
            new Book(4L, "Designing Data-Intensive Applications", "Martin Kleppmann", 2017)
    );

    private BookCatalog() {
    }

    public static List<Book> getBooks() {
        return BOOKS;
    }
}
