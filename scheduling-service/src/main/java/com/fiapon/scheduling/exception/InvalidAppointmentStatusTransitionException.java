package com.fiapon.scheduling.exception;

public class InvalidAppointmentStatusTransitionException extends RuntimeException {
    public InvalidAppointmentStatusTransitionException(String message) {
        super(message);
    }
}
