package com.fiapon.scheduling.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String notes;

    public Appointment() {
    }

    public Appointment(Long patientId, Long doctorId, LocalDateTime dateTime, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.notes = notes;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public UUID getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public void update(LocalDateTime dateTime, String notes) {
        this.dateTime = dateTime;
        this.notes = notes;
    }

    public void updateStatus(AppointmentStatus status) {
        this.status = status;
    }
}
