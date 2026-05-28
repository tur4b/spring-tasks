package org.example.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BaseResponse<T> {

    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    public BaseResponse(T data, String message) {
        this.data = data;
        this.message = message;
        this.timestamp =  LocalDateTime.now();
    }
}
