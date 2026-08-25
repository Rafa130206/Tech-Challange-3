package com.fiapon.scheduling.dto.appointment;

import java.time.LocalDateTime;

public record AppointmentRequest(
        Long patientId,
        Long doctorId,
        LocalDateTime dateTime,
        String notes
) {
}
