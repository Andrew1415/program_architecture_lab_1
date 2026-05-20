package com.example.andriuswebapp.service;

import java.util.List;

public record BookLocationStockResult(List<BookLocationStockView> items, boolean available) {

    public static BookLocationStockResult available(List<BookLocationStockView> items) {
        return new BookLocationStockResult(items, true);
    }

    public static BookLocationStockResult unavailable() {
        return new BookLocationStockResult(List.of(), false);
    }
}
