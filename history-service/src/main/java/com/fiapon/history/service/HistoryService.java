package com.fiapon.history.service;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.dto.HistoryResponse;
import com.fiapon.history.exceptions.HistoryNotFoundException;
import com.fiapon.history.model.History;
import com.fiapon.history.repository.HistoryRepository;
import com.fiapon.history.validation.HistoryValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                LocalDateTime.parse(request.date()),
                request.medicalRecords()
        );

        History saved = historyRepository.save(history);

        return HistoryResponse.from(saved);
    }

    public HistoryResponse update(HistoryRequest request, Long schedulingId){
        validators.forEach(v -> v.validate(request, schedulingId));
        History history = historyRepository.findBySchedulingId(schedulingId)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));

        history.update(request.doctorId(), LocalDateTime.parse(request.date()), request.medicalRecords());

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

    public HistoryResponse getBySchedulingId(Long schedulingId) {
        return historyRepository.findBySchedulingId(schedulingId)
                .map(HistoryResponse::from)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));
    }

    public void delete (Long schedulingId){
        History history = historyRepository.findBySchedulingId(schedulingId)
                .orElseThrow(() -> new HistoryNotFoundException(schedulingId));

        historyRepository.delete(history);
    }
}
