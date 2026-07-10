package com.example.library.exception;

/**
 * Thrown when attempting to borrow a copy that is already held by a borrower. Maps to HTTP 409.
 */
public class BookAlreadyBorrowedException extends RuntimeException {

    public BookAlreadyBorrowedException(Long bookId) {
        super("Book with id " + bookId + " is already borrowed");
    }
}
