package com.example.andriuswebapp.integration;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.repository.BookRepository;
import com.example.andriuswebapp.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PurchaseConcurrencyIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void oneHundredConcurrentPurchasesOnlyAllowOneSuccessForSingleRemainingItem() throws Exception {
        Book scarceBook = bookRepository.save(new Book("Last Copy", "Load Test", 2026, 1));
        int users = 100;
        CountDownLatch ready = new CountDownLatch(users);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(users);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < users; i++) {
            results.add(executorService.submit(() -> {
                ready.countDown();
                start.await();
                return bookService.purchaseBook(scarceBook.getId());
            }));
        }

        ready.await();
        start.countDown();
        executorService.shutdown();

        long successCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successCount += 1;
            }
        }

        Book updatedBook = bookRepository.findById(scarceBook.getId()).orElseThrow();
        assertThat(successCount).isEqualTo(1);
        assertThat(updatedBook.getStockQuantity()).isZero();
    }
}
