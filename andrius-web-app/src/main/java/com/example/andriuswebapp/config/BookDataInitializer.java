package com.example.andriuswebapp.config;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BookDataInitializer {

    @Bean
    CommandLineRunner seedBooks(BookRepository bookRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.saveAll(List.of(
                        new Book("Clean Code", "Robert C. Martin", 2008),
                        new Book("Refactoring", "Martin Fowler", 2018),
                        new Book("Domain-Driven Design", "Eric Evans", 2003),
                        new Book("Designing Data-Intensive Applications", "Martin Kleppmann", 2017)
                ));
            }
        };
    }
}
