package com.fiapon.scheduling.repository;

import com.fiapon.scheduling.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorIdAndDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
