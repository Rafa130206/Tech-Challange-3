package com.fiapon.scheduling.validation;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.exception.InvalidAppointmentDateException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class MinimumNoticeValidator implements AppointmentValidator {

    private final static int MINIMUM_NOTICE_DAYS = 3;

    @Override
    public void validate(AppointmentRequest request, UUID appointmentIdBeingUpdated) {
        LocalDateTime earliestAllowed = LocalDateTime.now().plusDays(MINIMUM_NOTICE_DAYS);
        if (request.dateTime().isBefore(earliestAllowed)) {
            throw new InvalidAppointmentDateException(
                    "Appointments must be scheduled at least " + MINIMUM_NOTICE_DAYS + " days in advance"
            );
        }
    }
}
