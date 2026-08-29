package com.fiapon.history.dto;

import java.time.LocalDateTime;

public record HistoryResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long schedulingId,
        LocalDateTime date,
        String medicalRecords
) {

    public static HistoryResponse from(com.fiapon.history.model.History history) {
        return new HistoryResponse(
                history.getId(),
                history.getPatientId(),
                history.getDoctorId(),
                history.getSchedulingId(),
                history.getDate(),
                history.getMedicalRecords()
        );
    }
}
