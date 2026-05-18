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
    void createBookInitializesLegacyStockToZero() {
        Book savedBook = new Book("9780134494166", "Clean Architecture", "Robert C. Martin", 2017, 0);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook("9780134494166", "Clean Architecture", "Robert C. Martin", 2017);

        assertThat(result.getStockQuantity()).isZero();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void getAllBooksReturnsRepositoryResultsInOrder() {
        Book first = new Book("9780321125217", "Domain-Driven Design", "Eric Evans", 2003, 5);
        Book second = new Book("9780134757599", "Refactoring", "Martin Fowler", 2018, 3);
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
        Book existingBook = new Book("9780000000001", "Old Title", "Old Author", 1999, 1);
        when(bookRepository.findById(7L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Book result = bookService.updateBook(7L, "9780000000007", "New Title", "New Author", 2026);

        assertThat(result.getIsbn()).isEqualTo("9780000000007");
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getStockQuantity()).isEqualTo(1);
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

        assertThatThrownBy(() -> bookService.updateBook(99L, "9780000000099", "Missing", "Nobody", 2024))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Book not found");
    }
}
