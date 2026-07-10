package com.example.library.exception;

/**
 * Thrown when registering a borrower whose email already exists. Maps to HTTP 409.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A borrower with email '" + email + "' already exists");
    }
}
