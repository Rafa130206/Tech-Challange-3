package com.fiapon.history.service;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.dto.HistoryResponse;
import com.fiapon.history.exceptions.HistoryNotFoundException;
import com.fiapon.history.exceptions.InvalidHistoryDataException;
import com.fiapon.history.model.History;
import com.fiapon.history.repository.HistoryRepository;
import com.fiapon.history.validation.HistoryValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final List<HistoryValidator> validators;

    public HistoryService(HistoryRepository historyRepository, List<HistoryValidator> validators){
        this.historyRepository = historyRepository;
        this.validators = validators;
    }

    public HistoryResponse create(HistoryRequest request){
        validators.forEach(v -> v.validate(request, null));
        History history = new History(
                request.patientId(),
                request.doctorId(),
                request.schedulingId(),
                parseDate(request.date()),
                request.medicalRecords()
        );

        History saved = historyRepository.save(history);

        return HistoryResponse.from(saved);
    }

    public HistoryResponse update(HistoryRequest request, String schedulingId){
        validators.forEach(v -> v.validate(request, schedulingId));
        History history = historyRepository.findBySchedulingId(schedulingId)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));

        history.update(request.doctorId(), parseDate(request.date()), request.medicalRecords());

        History saved = historyRepository.save(history);

        return HistoryResponse.from(saved);

    }

    public List<HistoryResponse> getAll() {
        return historyRepository.findAll().stream()
                .map(HistoryResponse::from)
                .toList();
    }

    public List<HistoryResponse> getByPatientId(Long patientId) {
        return historyRepository.findByPatientId(patientId).stream()
                .map(HistoryResponse::from)
                .toList();
    }

    public List<HistoryResponse> getByDoctorId(Long doctorId) {
        return historyRepository.findByDoctorId(doctorId).stream()
                .map(HistoryResponse::from)
                .toList();
    }

    public HistoryResponse getBySchedulingId(String schedulingId) {
        return historyRepository.findBySchedulingId(schedulingId)
                .map(HistoryResponse::from)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));
    }

    public void delete (String schedulingId){
        History history = historyRepository.findBySchedulingId(schedulingId)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));

        historyRepository.delete(history);
    }

    // Appointment/notification dates flow through the system as UTC-offset strings
    // (e.g. "2026-10-15T10:00:00Z"), but a plain "2026-10-15T10:00:00" should keep working
    // too. Try the offset form first, then fall back to a bare LocalDateTime.
    private LocalDateTime parseDate(String date) {
        try {
            return OffsetDateTime.parse(date).toLocalDateTime();
        } catch (DateTimeParseException offsetParseFailure) {
            try {
                return LocalDateTime.parse(date);
            } catch (DateTimeParseException localParseFailure) {
                throw new InvalidHistoryDataException("date '" + date + "' is not a valid ISO-8601 date-time");
            }
        }
    }
}
