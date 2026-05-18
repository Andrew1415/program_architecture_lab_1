package com.example.andriuswebapp.controller;

public class BookForm {

    private String isbn = "";
    private String title = "";
    private String author = "";
    private Integer year;

    public BookForm() {
    }

    public BookForm(String isbn, String title, String author, Integer year) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public static BookForm fromBook(com.example.andriuswebapp.model.Book book) {
        return new BookForm(book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear());
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
