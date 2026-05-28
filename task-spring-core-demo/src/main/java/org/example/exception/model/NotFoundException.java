package org.example.exception.model;

public class NotFoundException extends BaseRuntimeException {

    public NotFoundException(String message, ErrorResponse.ErrorPointer pointer) {
        super(message, pointer, ErrorResponse.ErrorType.NOT_FOUND);
    }
}
