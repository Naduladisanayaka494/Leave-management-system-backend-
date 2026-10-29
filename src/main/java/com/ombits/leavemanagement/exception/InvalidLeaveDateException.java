package com.ombits.leavemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLeaveDateException extends RuntimeException {
    public InvalidLeaveDateException(String message) {
        super(message);
    }
}
