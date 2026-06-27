package org.example.exception.model;

public class AccountLockedException extends SecurityException {

    public AccountLockedException(String message, ErrorResponse.ErrorPointer pointer) {
        super(message, pointer);
    }
}
