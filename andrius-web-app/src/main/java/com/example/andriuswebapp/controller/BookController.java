package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.service.BookCommentsResult;
import com.example.andriuswebapp.service.BookCommentsService;
import com.example.andriuswebapp.service.BookLocationStockResult;
import com.example.andriuswebapp.service.BookLocationStockService;
import com.example.andriuswebapp.service.BookService;
import com.example.andriuswebapp.service.ExternalBookSyncService;
import com.example.andriuswebapp.service.IsbnMetadata;
import com.example.andriuswebapp.service.IsbnMetadataService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
public class BookController {

    private final BookService bookService;
    private final BookCommentsService bookCommentsService;
    private final BookLocationStockService bookLocationStockService;
    private final ExternalBookSyncService externalBookSyncService;
    private final IsbnMetadataService isbnMetadataService;

    public BookController(BookService bookService, BookCommentsService bookCommentsService,
                          BookLocationStockService bookLocationStockService,
                          ExternalBookSyncService externalBookSyncService,
                          IsbnMetadataService isbnMetadataService) {
        this.bookService = bookService;
        this.bookCommentsService = bookCommentsService;
        this.bookLocationStockService = bookLocationStockService;
        this.externalBookSyncService = externalBookSyncService;
        this.isbnMetadataService = isbnMetadataService;
    }

    @GetMapping({"/", "/books"})
    public String books(Model model) {
        populateModel(model, new BookForm(), new CommentForm(), new StockForm(),
                null, null, null, null);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String viewBook(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(id);
            populateModel(model, new BookForm(), new CommentForm(), new StockForm(),
                    book, null, null, null);
            return "books";
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(id);
            populateModel(model, BookForm.fromBook(book), new CommentForm(), new StockForm(),
                    book, id, null, null);
            return "books";
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books")
    public String createBook(@ModelAttribute("bookForm") BookForm bookForm, Model model,
                             RedirectAttributes redirectAttributes) {
        String validationError = validate(bookForm, null);
        if (validationError != null) {
            populateModel(model, bookForm, new CommentForm(), new StockForm(),
                    null, null, null, null);
            model.addAttribute("errorMessage", validationError);
            return "books";
        }

        Book createdBook = bookService.createBook(
                normalize(bookForm.getIsbn()),
                normalize(bookForm.getTitle()),
                normalize(bookForm.getAuthor()),
                bookForm.getYear()
        );
        externalBookSyncService.syncBook(createdBook);
        redirectAttributes.addFlashAttribute("successMessage", "Book created.");
        return "redirect:/books";
    }

    @PostMapping("/books/fetch-metadata")
    public String fetchBookMetadata(@ModelAttribute("bookForm") BookForm bookForm, Model model) {
        Optional<IsbnMetadata> metadata = isbnMetadataService.findByIsbn(normalize(bookForm.getIsbn()));
        if (metadata.isPresent()) {
            applyMetadata(bookForm, metadata.get());
            populateModel(model, bookForm, new CommentForm(), new StockForm(),
                    null, null, null, null);
            model.addAttribute("successMessage", "Title, author, and release year loaded from ISBN.");
            return "books";
        }

        populateModel(model, bookForm, new CommentForm(), new StockForm(),
                null, null, null, null);
        model.addAttribute("errorMessage", "No public author or release year found for this ISBN.");
        return "books";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute("bookForm") BookForm bookForm, Model model,
                             RedirectAttributes redirectAttributes) {
        String validationError = validate(bookForm, id);
        if (validationError != null) {
            try {
                Book book = bookService.getBook(id);
                populateModel(model, bookForm, new CommentForm(), new StockForm(),
                        book, id, null, null);
                model.addAttribute("errorMessage", validationError);
                return "books";
            } catch (NoSuchElementException exception) {
                redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
                return "redirect:/books";
            }
        }

        try {
            Book updatedBook = bookService.updateBook(
                    id,
                    normalize(bookForm.getIsbn()),
                    normalize(bookForm.getTitle()),
                    normalize(bookForm.getAuthor()),
                    bookForm.getYear()
            );
            externalBookSyncService.syncBook(updatedBook);
            redirectAttributes.addFlashAttribute("successMessage", "Book updated.");
            return "redirect:/books/" + id;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books/{id}/fetch-metadata")
    public String fetchBookMetadataForEdit(@PathVariable Long id, @ModelAttribute("bookForm") BookForm bookForm,
                                           Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(id);
            Optional<IsbnMetadata> metadata = isbnMetadataService.findByIsbn(normalize(bookForm.getIsbn()));
            if (metadata.isPresent()) {
                applyMetadata(bookForm, metadata.get());
                populateModel(model, bookForm, new CommentForm(), new StockForm(),
                        book, id, null, null);
                model.addAttribute("successMessage", "Title, author, and release year loaded from ISBN.");
                return "books";
            }

            populateModel(model, bookForm, new CommentForm(), new StockForm(),
                    book, id, null, null);
            model.addAttribute("errorMessage", "No public author or release year found for this ISBN.");
            return "books";
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted.");
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
        }
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/purchase")
    public String purchaseBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean purchased = bookService.purchaseBook(id);
            if (purchased) {
                redirectAttributes.addFlashAttribute("successMessage", "Purchase completed.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Book is out of stock.");
            }
            return "redirect:/books/" + id;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books/{id}/comments")
    public String createComment(@PathVariable Long id, @ModelAttribute("commentForm") CommentForm commentForm,
                                Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            Book book = bookService.getBook(id);
            String validationError = validateComment(commentForm);
            if (validationError != null) {
                populateModel(model, new BookForm(), commentForm, new StockForm(),
                        book, null, validationError, null);
                return "books";
            }

            if (!externalBookSyncService.ensureCommentsBookExists(book)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Comments API is unavailable right now.");
                return "redirect:/books/" + id;
            }

            boolean created = bookCommentsService.createCommentForIsbn(
                    book.getIsbn(),
                    resolveReviewerName(authentication),
                    normalize(commentForm.getContent()),
                    commentForm.getRating()
            );
            if (created) {
                redirectAttributes.addFlashAttribute("successMessage", "Comment created.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Could not create comment right now.");
            }
            return "redirect:/books/" + id;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @GetMapping("/books/{bookId}/stocks/{stockId}/edit")
    public String editLocationStock(@PathVariable Long bookId, @PathVariable Long stockId,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(bookId);
            BookLocationStockResult stockResult = bookLocationStockService.getStockForIsbn(book.getIsbn());
            if (!stockResult.available()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Stock API is unavailable right now.");
                return "redirect:/books/" + bookId;
            }

            return stockResult.items().stream()
                    .filter(stock -> stock.id().equals(stockId))
                    .findFirst()
                    .map(stock -> {
                        populateModel(model, new BookForm(), new CommentForm(), StockForm.fromView(stock),
                                book, null, null, stockId);
                        return "books";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Location stock not found.");
                        return "redirect:/books/" + bookId;
                    });
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books/{id}/stocks")
    public String createLocationStock(@PathVariable Long id, @ModelAttribute("stockForm") StockForm stockForm,
                                      Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(id);
            String validationError = validateStock(stockForm);
            if (validationError != null) {
                populateModel(model, new BookForm(), new CommentForm(), stockForm,
                        book, null, null, null);
                model.addAttribute("stockErrorMessage", validationError);
                return "books";
            }

            if (!externalBookSyncService.ensureStockBookExists(book)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Stock API is unavailable right now.");
                return "redirect:/books/" + id;
            }

            boolean created = bookLocationStockService.createStockForIsbn(
                    book.getIsbn(),
                    normalize(stockForm.getLocationName()),
                    stockForm.getQuantity()
            );
            if (created) {
                redirectAttributes.addFlashAttribute("successMessage", "Location stock created.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Could not create location stock right now.");
            }
            return "redirect:/books/" + id;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books/{bookId}/stocks/{stockId}")
    public String updateLocationStock(@PathVariable Long bookId, @PathVariable Long stockId,
                                      @ModelAttribute("stockForm") StockForm stockForm,
                                      Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(bookId);
            String validationError = validateStock(stockForm);
            if (validationError != null) {
                populateModel(model, new BookForm(), new CommentForm(), stockForm,
                        book, null, null, stockId);
                model.addAttribute("stockErrorMessage", validationError);
                return "books";
            }

            if (!externalBookSyncService.ensureStockBookExists(book)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Stock API is unavailable right now.");
                return "redirect:/books/" + bookId;
            }

            boolean updated = bookLocationStockService.updateStock(
                    stockId,
                    book.getIsbn(),
                    normalize(stockForm.getLocationName()),
                    stockForm.getQuantity()
            );
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "Location stock updated.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Could not update location stock right now.");
            }
            return "redirect:/books/" + bookId;
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    private void populateModel(Model model, BookForm bookForm, CommentForm commentForm,
                               StockForm stockForm, Book selectedBook, Long editingBookId,
                               String commentErrorMessage, Long editingStockId) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("bookForm", bookForm);
        model.addAttribute("commentForm", commentForm);
        model.addAttribute("stockForm", stockForm);
        model.addAttribute("selectedBook", selectedBook);
        model.addAttribute("editingBookId", editingBookId);
        model.addAttribute("commentErrorMessage", commentErrorMessage);
        model.addAttribute("editingStockId", editingStockId);
        if (selectedBook != null) {
            BookCommentsResult commentsResult = bookCommentsService.getCommentsForIsbn(selectedBook.getIsbn());
            BookLocationStockResult stockResult = bookLocationStockService.getStockForIsbn(selectedBook.getIsbn());
            model.addAttribute("bookComments", commentsResult.comments());
            model.addAttribute("bookCommentsAvailable", commentsResult.available());
            model.addAttribute("bookLocationStocks", stockResult.items());
            model.addAttribute("bookLocationStocksAvailable", stockResult.available());
            model.addAttribute("bookLocationStockTotal",
                    stockResult.items().stream().mapToInt(com.example.andriuswebapp.service.BookLocationStockView::quantity).sum());
        } else {
            model.addAttribute("bookComments", java.util.List.of());
            model.addAttribute("bookCommentsAvailable", true);
            model.addAttribute("bookLocationStocks", java.util.List.of());
            model.addAttribute("bookLocationStocksAvailable", true);
            model.addAttribute("bookLocationStockTotal", 0);
        }
    }

    private String validate(BookForm bookForm, Long editingBookId) {
        String normalizedIsbn = normalize(bookForm.getIsbn());
        if (normalizedIsbn.isEmpty()) {
            return "ISBN is required.";
        }
        if (normalize(bookForm.getTitle()).isEmpty()) {
            return "Title is required.";
        }
        if (normalize(bookForm.getAuthor()).isEmpty()) {
            return "Author is required.";
        }
        if (bookForm.getYear() == null) {
            return "Year is required.";
        }
        int currentYear = Year.now().getValue() + 1;
        if (bookForm.getYear() < 1000 || bookForm.getYear() > currentYear) {
            return "Year must be between 1000 and " + currentYear + ".";
        }
        if (editingBookId == null && bookService.isbnExists(normalizedIsbn)) {
            return "ISBN must be unique.";
        }
        if (editingBookId != null && bookService.isbnExistsForAnotherBook(normalizedIsbn, editingBookId)) {
            return "ISBN must be unique.";
        }

        return null;
    }

    private String validateComment(CommentForm commentForm) {
        if (normalize(commentForm.getContent()).isEmpty()) {
            return "Comment text is required.";
        }
        if (commentForm.getRating() == null) {
            return "Rating is required.";
        }
        if (commentForm.getRating() < 1 || commentForm.getRating() > 5) {
            return "Rating must be between 1 and 5.";
        }
        return null;
    }

    private String validateStock(StockForm stockForm) {
        if (normalize(stockForm.getLocationName()).isEmpty()) {
            return "Location name is required.";
        }
        if (stockForm.getQuantity() == null) {
            return "Quantity is required.";
        }
        if (stockForm.getQuantity() < 0 || stockForm.getQuantity() > 100000) {
            return "Quantity must be between 0 and 100000.";
        }
        return null;
    }

    private void applyMetadata(BookForm bookForm, IsbnMetadata metadata) {
        if (!metadata.title().isBlank()) {
            bookForm.setTitle(metadata.title());
        }
        if (!metadata.author().isBlank()) {
            bookForm.setAuthor(metadata.author());
        }
        if (metadata.year() != null) {
            bookForm.setYear(metadata.year());
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveReviewerName(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Authenticated user";
    }
}
