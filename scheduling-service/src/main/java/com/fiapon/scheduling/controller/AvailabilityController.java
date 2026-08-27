package com.fiapon.scheduling.controller;

import com.fiapon.scheduling.availability.AvailabilityService;
import com.fiapon.scheduling.dto.availability.AvailableSlotsResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    public List<AvailableSlotsResponse> getAvailability(
            @RequestParam(required = false) Long doctorId,
            @RequestParam LocalDate date) {
        if (doctorId != null) {
            return List.of(availabilityService.getAvailability(doctorId, date));
        }
        return availabilityService.getAvailabilityForAllDoctors(date);
    }
}