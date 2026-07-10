package com.example.library.exception;

/**
 * Thrown when returning a copy that is not currently borrowed by anyone. Maps to HTTP 409.
 */
public class BookNotBorrowedException extends RuntimeException {

    public BookNotBorrowedException(Long bookId) {
        super("Book with id " + bookId + " is not currently borrowed");
    }
}
