package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping({"/", "/books"})
    public String books(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "books";
    }
}
