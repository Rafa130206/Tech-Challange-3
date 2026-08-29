package com.fiapon.history.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "histories")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false, unique = true)
    private Long schedulingId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, columnDefinition = "text")
    private String medicalRecords;

    public History(){
    }

    public History(Long patientId, Long doctorId, Long schedulingId, LocalDateTime date, String medicalRecords) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.schedulingId = schedulingId;
        this.date = date;
        this.medicalRecords = medicalRecords;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public Long getSchedulingId() {
        return schedulingId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMedicalRecords() {
        return medicalRecords;
    }

    public void update(LocalDateTime dateTime, String records) {
        this.date = dateTime;
        this.medicalRecords = records;
    }

}
