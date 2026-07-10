package com.example.library.exception;

/**
 * Thrown when a book is registered with an ISBN that already exists but with a different
 * title or author, which would violate ISBN consistency. Maps to HTTP 409.
 */
public class IsbnConflictException extends RuntimeException {

    public IsbnConflictException(String isbn, String existingTitle, String existingAuthor) {
        super("ISBN '" + isbn + "' is already registered as '" + existingTitle
                + "' by '" + existingAuthor + "'; title and author must match for the same ISBN");
    }
}
