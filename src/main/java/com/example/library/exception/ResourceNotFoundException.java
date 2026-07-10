package com.example.library.exception;

/**
 * Thrown when a requested borrower or book id does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " was not found");
    }
}
