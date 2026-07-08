package com.example.library.dto;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        boolean available,
        Long borrowerId
) {
}
