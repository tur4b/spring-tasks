package org.example.exception.model;

public abstract class SecurityException extends BaseRuntimeException {

    public SecurityException(String message, ErrorResponse.ErrorPointer pointer) {
        super(message, pointer, ErrorResponse.ErrorType.UNAUTHORIZED);
    }

}
