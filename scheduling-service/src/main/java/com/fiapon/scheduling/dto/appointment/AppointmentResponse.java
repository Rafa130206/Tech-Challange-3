package com.fiapon.scheduling.dto.appointment;

import com.fiapon.scheduling.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
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
