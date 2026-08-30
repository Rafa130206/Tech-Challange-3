package com.fiapon.scheduling.service;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.dto.appointment.AppointmentResponse;
import com.fiapon.scheduling.exception.AppointmentNotFoundException;
import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.repository.AppointmentRepository;
import com.fiapon.scheduling.validation.AppointmentValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final List<AppointmentValidator> validators;

    public AppointmentService(AppointmentRepository appointmentRepository, List<AppointmentValidator> validators) {
        this.appointmentRepository = appointmentRepository;
        this.validators = validators;
    }

    public AppointmentResponse create(AppointmentRequest request) {
        validators.forEach(v -> v.validate(request, null));
        Appointment appointment = new Appointment(
                request.patientId(),
                request.doctorId(),
                request.dateTime(),
                request.notes()
        );
        Appointment saved = appointmentRepository.save(appointment);
        // TODO publish AppointmentEvent(CREATED)
        return AppointmentResponse.from(saved);
    }

    public AppointmentResponse update(UUID id, AppointmentRequest request) {
        validators.forEach(v -> v.validate(request, id));
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.update(request.dateTime(), request.notes());
        Appointment saved = appointmentRepository.save(appointment);
        // TODO publish AppointmentEvent(UPDATED)
        return AppointmentResponse.from(saved);
    }

    public AppointmentResponse getById(UUID id) {
        return appointmentRepository.findById(id)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    public List<AppointmentResponse> getAll() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public List<AppointmentResponse> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }
}