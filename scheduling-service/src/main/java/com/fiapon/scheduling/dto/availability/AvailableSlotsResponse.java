package com.fiapon.scheduling.dto.availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailableSlotsResponse(
        Long doctorId,
        LocalDate date,
        List<LocalTime> availableSlots
) {}