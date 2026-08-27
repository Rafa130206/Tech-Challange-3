package com.fiapon.scheduling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ProblemDetail handleAppointmentNotFound(AppointmentNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Appointment Not Found");
        return problem;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(UsernameNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("User Not Found");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action"
        );
        problem.setTitle("Access Denied");
        return problem;
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ProblemDetail handleAppointmentConflict(AppointmentConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Appointment Conflict");
        return problem;
    }

    @ExceptionHandler(InvalidAppointmentDateException.class)
    public ProblemDetail handleInvalidAppointmentDate(InvalidAppointmentDateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Appointment Date");
        return problem;
    }

}