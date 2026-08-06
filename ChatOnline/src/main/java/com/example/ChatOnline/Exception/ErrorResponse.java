package com.example.ChatOnline.Exception;

import lombok.Builder;

@Builder
public class ErrorResponse {
    private int code;

    private int status;

    private String error;

    private String message;

    private String path;
}
