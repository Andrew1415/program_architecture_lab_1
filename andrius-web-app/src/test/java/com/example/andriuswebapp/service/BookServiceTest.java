package com.example.andriuswebapp.service;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void createBookStoresProvidedStockQuantity() {
        Book savedBook = new Book("Clean Architecture", "Robert C. Martin", 2017, 7);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook("Clean Architecture", "Robert C. Martin", 2017, 7);

        assertThat(result.getStockQuantity()).isEqualTo(7);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void getAllBooksReturnsRepositoryResultsInOrder() {
        Book first = new Book("Domain-Driven Design", "Eric Evans", 2003, 5);
        Book second = new Book("Refactoring", "Martin Fowler", 2018, 3);
        when(bookRepository.findAllByOrderByIdAsc()).thenReturn(List.of(first, second));

        List<Book> result = bookService.getAllBooks();

        assertThat(result).containsExactly(first, second);
        verify(bookRepository).findAllByOrderByIdAsc();
    }

    @Test
    void getBookThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBook(55L))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Book not found: 55");
    }

    @Test
    void updateBookReplacesExistingFieldsAndSavesBook() {
        Book existingBook = new Book("Old Title", "Old Author", 1999, 1);
        when(bookRepository.findById(7L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Book result = bookService.updateBook(7L, "New Title", "New Author", 2026, 9);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getStockQuantity()).isEqualTo(9);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void purchaseBookReturnsFalseWhenItemIsOutOfStock() {
        when(bookRepository.existsById(10L)).thenReturn(true);
        when(bookRepository.decrementStockIfAvailable(10L)).thenReturn(0);

        boolean purchased = bookService.purchaseBook(10L);

        assertThat(purchased).isFalse();
        verify(bookRepository).decrementStockIfAvailable(10L);
    }

    @Test
    void purchaseBookReturnsTrueWhenStockIsDecremented() {
        when(bookRepository.existsById(11L)).thenReturn(true);
        when(bookRepository.decrementStockIfAvailable(11L)).thenReturn(1);

        boolean purchased = bookService.purchaseBook(11L);

        assertThat(purchased).isTrue();
        verify(bookRepository).decrementStockIfAvailable(11L);
    }

    @Test
    void deleteBookRemovesExistingRecord() {
        when(bookRepository.existsById(12L)).thenReturn(true);

        bookService.deleteBook(12L);

        verify(bookRepository).deleteById(12L);
    }

    @Test
    void deleteBookThrowsWhenBookDoesNotExist() {
        when(bookRepository.existsById(13L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(13L))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Book not found: 13");
    }

    @Test
    void updateBookThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, "Missing", "Nobody", 2024, 1))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Book not found");
    }
}
