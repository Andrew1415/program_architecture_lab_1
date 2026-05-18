package com.example.andriuswebapp.integration;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void createBookPersistsNewRecord() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user("admin@ku.lt").roles("ADMIN"))
                        .with(csrf())
                        .param("isbn", "9780132350884")
                        .param("title", "Performance Engineering")
                        .param("author", "Andrius Student")
                        .param("year", "2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("successMessage", "Book created."));

        assertThat(bookRepository.findAllByOrderByIdAsc())
                .anySatisfy(book -> {
                    assertThat(book.getIsbn()).isEqualTo("9780132350884");
                    assertThat(book.getTitle()).isEqualTo("Performance Engineering");
                    assertThat(book.getStockQuantity()).isZero();
                });
    }

    @Test
    void purchaseBookReducesStockByOne() throws Exception {
        Book book = bookRepository.save(new Book("9781111111111", "Concurrent Systems", "A. Tester", 2024, 2));

        mockMvc.perform(post("/books/{id}/purchase", book.getId())
                        .with(user("user@ku.lt").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + book.getId()))
                .andExpect(flash().attribute("successMessage", "Purchase completed."));

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getStockQuantity()).isEqualTo(1);
    }
}
