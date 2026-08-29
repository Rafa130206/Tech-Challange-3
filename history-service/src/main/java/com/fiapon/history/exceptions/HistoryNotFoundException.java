package com.fiapon.history.exceptions;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException(Long id) {
        super("History not found: " + id);
    }
}
