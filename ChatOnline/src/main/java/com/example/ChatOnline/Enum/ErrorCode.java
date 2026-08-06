package com.example.ChatOnline.Enum;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(500, "Unknown Exception !", HttpStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(403, "Access denied !", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(401, "Authentication is required !", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(500, "Can not generate token !", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(400, "User already existed !", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(404, "User not found !", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
