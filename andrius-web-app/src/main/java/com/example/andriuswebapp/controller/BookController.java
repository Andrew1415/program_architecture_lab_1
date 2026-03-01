package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.model.BookCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookController {

    @GetMapping({"/", "/books"})
    public String books(Model model) {
        model.addAttribute("books", BookCatalog.getBooks());
        return "books";
    }
}
