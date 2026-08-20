package com.shopzone.user.web;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final HttpStatus status;

    public BusinessException(String messageKey, HttpStatus status) {
        super(messageKey);
        this.messageKey = messageKey;
        this.status = status;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException notFound(String key) {
        return new BusinessException(key, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String key) {
        return new BusinessException(key, HttpStatus.CONFLICT);
    }

    public static BusinessException unauthorized(String key) {
        return new BusinessException(key, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException badRequest(String key) {
        return new BusinessException(key, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException forbidden(String key) {
        return new BusinessException(key, HttpStatus.FORBIDDEN);
    }
}
