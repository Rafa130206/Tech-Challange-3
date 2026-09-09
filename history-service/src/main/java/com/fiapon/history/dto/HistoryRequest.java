package com.fiapon.history.dto;

public record HistoryRequest(
        Long patientId,
        Long doctorId,
        String schedulingId,
        String date,
        String medicalRecords
) {
}
