package com.fiapon.history.dto;

public record HistoryRequest(
        Long patientId,
        Long doctorId,
        Long schedulingId,
        String date,
        String medicalRecords
) {
}
