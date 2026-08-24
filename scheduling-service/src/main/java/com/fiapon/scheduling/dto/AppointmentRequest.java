package com.fiapon.scheduling.dto;

import java.time.LocalDateTime;

public record AppointmentRequest(
        Long patientId,
        Long doctorId,
        LocalDateTime dateTime,
        String notes
) {
}
