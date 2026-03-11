package com.example.andriuswebapp.controller;

public class BookForm {

    private String title = "";
    private String author = "";
    private Integer year;
    private Integer stockQuantity = 1;

    public BookForm() {
    }

    public BookForm(String title, String author, Integer year, Integer stockQuantity) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.stockQuantity = stockQuantity;
    }

    public static BookForm fromBook(com.example.andriuswebapp.model.Book book) {
        return new BookForm(book.getTitle(), book.getAuthor(), book.getYear(), book.getStockQuantity());
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

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
