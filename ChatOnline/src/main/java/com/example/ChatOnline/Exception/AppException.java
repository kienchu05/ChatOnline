package com.example.ChatOnline.Exception;

import com.example.ChatOnline.Enum.ErrorCode;

public class AppException extends RuntimeException{
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
