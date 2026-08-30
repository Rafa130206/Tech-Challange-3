package com.fiapon.scheduling.controller;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.dto.appointment.AppointmentResponse;
import com.fiapon.scheduling.security.CustomUserDetails;
import com.fiapon.scheduling.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('NURSE')")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@RequestBody AppointmentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse update(@PathVariable UUID id, @RequestBody AppointmentRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<AppointmentResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public AppointmentResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> getMyAppointments(@AuthenticationPrincipal CustomUserDetails principal) {
        return service.getByPatient(principal.getId());
    }

}
