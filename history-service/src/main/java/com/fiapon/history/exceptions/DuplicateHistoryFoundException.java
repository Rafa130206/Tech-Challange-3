package com.fiapon.history.exceptions;

public class DuplicateHistoryFoundException extends RuntimeException {
    public DuplicateHistoryFoundException(String message) {
        super(message);
    }
}
