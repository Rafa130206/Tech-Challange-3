package com.fiapon.scheduling.availability;

import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkingHoursSlotGenerator {

    private static final LocalTime START = LocalTime.of(7, 0);
    private static final LocalTime END = LocalTime.of(19, 0);
    private static final int SLOT_DURATION_HOURS = 1;

    public List<LocalTime> generate() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = START;
        while (current.isBefore(END)) {
            slots.add(current);
            current = current.plusHours(SLOT_DURATION_HOURS);
        }
        return slots;
    }
}