package com.fiapon.history.validation;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.exceptions.DuplicateHistoryFoundException;
import com.fiapon.history.repository.HistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class UniqueHistorySchedulingValidator implements HistoryValidator {

    private final HistoryRepository historyRepository;

    public UniqueHistorySchedulingValidator(HistoryRepository repository) {
        this.historyRepository = repository;
    }

    @Override
    public  void validate(HistoryRequest request, Long schedulingId){

        historyRepository
                .findBySchedulingId(request.schedulingId())
                .filter(existing -> !existing.getSchedulingId().equals(schedulingId))
                .ifPresent(existing -> {
                    throw new DuplicateHistoryFoundException(
                            "Scheduling " + request.schedulingId() + " already has a history record");
                });

    }
}
