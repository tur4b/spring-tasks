package org.example.exception.model;

import lombok.Getter;

import java.util.List;

public record ErrorResponse(
            ErrorType type,
            String title,
            List<Error> errors
    ) {

    public static ErrorResponse of(ErrorType type, String title, List<Error> errors) {
        return new ErrorResponse(type, title, errors);
    }

    public static ErrorResponse of(ErrorType type, List<Error> errors) {
        return new ErrorResponse(type, type.identifier, errors);
    }

    public static ErrorResponse of(ErrorType type) {
        return new ErrorResponse(type, type.identifier, null);
    }

    public record Error(
            String detail,
            String pointer
    ) {}

    @Getter
    public enum ErrorType {
        VALIDATION_ERROR("Validation error"),
        NOT_FOUND("Not found"),
        UNAUTHORIZED("Unauthorized");

        private final String identifier;

        ErrorType(String identifier) {
            this.identifier = identifier;
        }
    }

    public enum ErrorPointer {
        id,
        credentials, username
    }
}