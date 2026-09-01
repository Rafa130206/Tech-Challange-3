package com.fiapon.history.validation;

import com.fiapon.history.dto.HistoryRequest;

public interface HistoryValidator {

    void validate(HistoryRequest request, Long schedulingId);
}
