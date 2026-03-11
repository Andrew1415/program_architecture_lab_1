package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.model.Book;
import com.example.andriuswebapp.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.NoSuchElementException;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping({"/", "/books"})
    public String books(Model model) {
        populateModel(model, new BookForm(), null);
        return "books";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBook(id);
            populateModel(model, BookForm.fromBook(book), id);
            return "books";
        } catch (NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
        }
    }

    @PostMapping("/books")
    public String createBook(@ModelAttribute("bookForm") BookForm bookForm, Model model,
                             RedirectAttributes redirectAttributes) {
        String validationError = validate(bookForm);
        if (validationError != null) {
            populateModel(model, bookForm, null);
            model.addAttribute("errorMessage", validationError);
            return "books";
        }

        bookService.createBook(normalize(bookForm.getTitle()), normalize(bookForm.getAuthor()), bookForm.getYear());
        redirectAttributes.addFlashAttribute("successMessage", "Book created.");
        return "redirect:/books";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute("bookForm") BookForm bookForm, Model model,
                             RedirectAttributes redirectAttributes) {
        String validationError = validate(bookForm);
        if (validationError != null) {
            populateModel(model, bookForm, id);
            model.addAttribute("errorMessage", validationError);
            return "books";
        }

        try {
            bookService.updateBook(id, normalize(bookForm.getTitle()), normalize(bookForm.getAuthor()), bookForm.getYear());
            redirectAttributes.addFlashAttribute("successMessage", "Book updated.");
            return "redirect:/books";
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

    private void populateModel(Model model, BookForm bookForm, Long editingBookId) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("bookForm", bookForm);
        model.addAttribute("editingBookId", editingBookId);
    }

    private String validate(BookForm bookForm) {
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

        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
