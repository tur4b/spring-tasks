package org.example.exception.model;

import lombok.Getter;

@Getter
public abstract class BaseRuntimeException extends RuntimeException {

    protected final ErrorResponse.ErrorType type;
    protected final ErrorResponse.ErrorPointer pointer;

    public BaseRuntimeException(String message,
                                ErrorResponse.ErrorPointer pointer,
                                ErrorResponse.ErrorType type) {
        super(message);
        this.pointer = pointer;
        this.type = type;

    }


}
