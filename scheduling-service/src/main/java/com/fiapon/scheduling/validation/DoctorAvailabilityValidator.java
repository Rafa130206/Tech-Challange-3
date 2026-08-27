package com.fiapon.scheduling.validation;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.exception.AppointmentConflictException;
import com.fiapon.scheduling.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DoctorAvailabilityValidator implements AppointmentValidator {

    private static final long MINIMUM_GAP_HOURS = 1;

    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityValidator(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void validate(AppointmentRequest request, Long appointmentIdBeingUpdated) {
        LocalDateTime windowStart = request.dateTime().minusHours(MINIMUM_GAP_HOURS);
        LocalDateTime windowEnd = request.dateTime().plusHours(MINIMUM_GAP_HOURS);

        boolean hasConflict = appointmentRepository
                .findByDoctorIdAndDateTimeBetween(request.doctorId(), windowStart, windowEnd)
                .stream()
                .anyMatch(existing -> !existing.getId().equals(appointmentIdBeingUpdated));

        if (hasConflict) {
            throw new AppointmentConflictException(
                    "Doctor already has an appointment within 1 hour of " + request.dateTime()
            );
        }
    }
}