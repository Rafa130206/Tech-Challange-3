package com.fiapon.scheduling.dto;

import com.fiapon.scheduling.model.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDateTime dateTime,
        AppointmentStatus status,
        String notes
) {
    public static AppointmentResponse from(com.fiapon.scheduling.model.Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                appointment.getStatus(),
                appointment.getNotes()
        );
    }
}
