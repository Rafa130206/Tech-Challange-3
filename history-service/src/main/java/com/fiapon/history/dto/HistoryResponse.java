package com.fiapon.history.dto;

import java.time.LocalDateTime;

public record HistoryResponse(
        Long patientId,
        Long doctorId,
        Long schedulingId,
        LocalDateTime date,
        String records
) {

    public static HistoryResponse from(com.fiapon.history.model.History history) {
        return new HistoryResponse(
                history.getPatientId(),
                history.getDoctorId(),
                history.getSchedulingId(),
                history.getDate(),
                history.getMedicalRecords()
        );
    }
}
