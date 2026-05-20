package com.example.andriuswebapp.service;

import java.util.List;

public record BookCommentsResult(List<BookCommentView> comments, boolean available) {

    public static BookCommentsResult available(List<BookCommentView> comments) {
        return new BookCommentsResult(comments, true);
    }

    public static BookCommentsResult unavailable() {
        return new BookCommentsResult(List.of(), false);
    }
}
