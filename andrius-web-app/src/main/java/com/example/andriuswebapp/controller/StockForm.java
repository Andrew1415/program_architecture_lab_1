package com.example.andriuswebapp.controller;

import com.example.andriuswebapp.service.BookLocationStockView;

public class StockForm {

    private String locationName;
    private Integer quantity;

    public static StockForm fromView(BookLocationStockView stockView) {
        StockForm form = new StockForm();
        form.setLocationName(stockView.locationName());
        form.setQuantity(stockView.quantity());
        return form;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
