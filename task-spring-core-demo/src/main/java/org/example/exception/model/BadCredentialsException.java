package org.example.exception.model;

public class BadCredentialsException extends SecurityException {
    public BadCredentialsException(String message, ErrorResponse.ErrorPointer pointer) {
        super(message, pointer);
    }
}
