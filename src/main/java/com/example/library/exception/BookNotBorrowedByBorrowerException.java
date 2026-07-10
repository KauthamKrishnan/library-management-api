package com.example.library.exception;

/**
 * Thrown when a borrower tries to return a copy that is currently held by a different
 * borrower. Maps to HTTP 409.
 */
public class BookNotBorrowedByBorrowerException extends RuntimeException {

    public BookNotBorrowedByBorrowerException(Long bookId, Long borrowerId) {
        super("Book with id " + bookId + " is not borrowed by borrower with id " + borrowerId);
    }
}
