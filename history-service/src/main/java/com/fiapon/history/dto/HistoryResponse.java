package com.fiapon.history.dto;

public record HistoryResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long schedulingId,
        String date,
        String medicalRecords
) {

    public static HistoryResponse from(com.fiapon.history.model.History history) {
        return new HistoryResponse(
                history.getId(),
                history.getPatientId(),
                history.getDoctorId(),
                history.getSchedulingId(),
                history.getDate().toString(),
                history.getMedicalRecords()
        );
    }
}
