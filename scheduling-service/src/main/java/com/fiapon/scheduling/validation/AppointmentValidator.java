package com.fiapon.scheduling.validation;

import com.fiapon.scheduling.dto.AppointmentRequest;

public interface AppointmentValidator {
    void validate(AppointmentRequest request, Long appointmentIdBeingUpdated);
}
