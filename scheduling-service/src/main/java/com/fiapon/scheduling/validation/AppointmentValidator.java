package com.fiapon.scheduling.validation;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;

import java.util.UUID;

public interface AppointmentValidator {
    void validate(AppointmentRequest request, UUID appointmentIdBeingUpdated);
}
