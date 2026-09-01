package com.fiapon.history.repository;

import com.fiapon.history.model.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findByPatientId(Long patientId);

    List<History> findByDoctorId(Long doctorId);

    Optional<History> findBySchedulingId(Long schedulingId);

    List<History> findByDate (LocalDateTime relatedDate);
}
