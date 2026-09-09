package com.fiapon.scheduling.dto.appointment;

import com.fiapon.scheduling.model.AppointmentStatus;

public record AppointmentStatusRequest(
        AppointmentStatus status
) {
}
