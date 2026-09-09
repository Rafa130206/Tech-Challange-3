package com.fiapon.scheduling.repository;

import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorIdAndDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    // Excludes a given status (CANCELLED) so a freed-up slot doesn't keep looking booked.
    List<Appointment> findByDoctorIdAndDateTimeBetweenAndStatusNot(
            Long doctorId, LocalDateTime start, LocalDateTime end, AppointmentStatus excludedStatus);
}
