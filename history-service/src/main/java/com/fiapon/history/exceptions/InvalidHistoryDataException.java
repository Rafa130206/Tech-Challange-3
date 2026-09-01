package com.fiapon.history.exceptions;

public class InvalidHistoryDataException extends RuntimeException {
    public InvalidHistoryDataException(String message) {super("The request contains invalid data: " + message);}
}
