package com.example.andriuswebapp.repository;

import com.example.andriuswebapp.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByOrderByIdAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.stockQuantity = b.stockQuantity - 1 where b.id = :id and b.stockQuantity > 0")
    int decrementStockIfAvailable(@Param("id") Long id);
}
