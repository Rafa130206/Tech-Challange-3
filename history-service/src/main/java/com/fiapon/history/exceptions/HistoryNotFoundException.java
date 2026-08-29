package com.fiapon.history.exceptions;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException(String message) {
        super("History not found: " + message);
    }
}
