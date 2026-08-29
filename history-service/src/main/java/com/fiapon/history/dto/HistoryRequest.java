package com.fiapon.history.dto;

import java.time.LocalDateTime;

public record HistoryRequest(
        Long patientId,
        Long doctorId,
        Long schedulingId,
        LocalDateTime date,
        String medicalRecords
) {
}
