package com.fiapon.scheduling.controller;

import com.fiapon.scheduling.dto.AppointmentRequest;
import com.fiapon.scheduling.dto.AppointmentResponse;
import com.fiapon.scheduling.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@RequestBody AppointmentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public AppointmentResponse update(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<AppointmentResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public AppointmentResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> getMyAppointments(Authentication authentication) {
        Long patientId = Long.valueOf(authentication.getName());
        return service.getByPatient(patientId);
    }

}
