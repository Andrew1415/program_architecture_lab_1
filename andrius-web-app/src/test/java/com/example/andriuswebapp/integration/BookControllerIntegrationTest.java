package com.example.andriuswebapp.integration;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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
                        .param("title", "Performance Engineering")
                        .param("author", "Andrius Student")
                        .param("year", "2026")
                        .param("stockQuantity", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("successMessage", "Book created."));

        assertThat(bookRepository.findAllByOrderByIdAsc())
                .anySatisfy(book -> {
                    assertThat(book.getTitle()).isEqualTo("Performance Engineering");
                    assertThat(book.getStockQuantity()).isEqualTo(4);
                });
    }

    @Test
    void purchaseBookReducesStockByOne() throws Exception {
        Book book = bookRepository.save(new Book("Concurrent Systems", "A. Tester", 2024, 2));

        mockMvc.perform(post("/books/{id}/purchase", book.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("successMessage", "Purchase completed."));

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getStockQuantity()).isEqualTo(1);
    }
}
