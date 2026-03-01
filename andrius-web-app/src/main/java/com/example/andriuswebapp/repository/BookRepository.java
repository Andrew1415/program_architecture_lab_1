package com.example.andriuswebapp.repository;

import com.example.andriuswebapp.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
