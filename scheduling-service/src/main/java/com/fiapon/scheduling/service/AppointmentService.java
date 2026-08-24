package com.fiapon.scheduling.service;

import com.fiapon.scheduling.dto.AppointmentRequest;
import com.fiapon.scheduling.dto.AppointmentResponse;
import com.fiapon.scheduling.exception.AppointmentNotFoundException;
import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public AppointmentResponse create(AppointmentRequest request) {
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

    public AppointmentResponse update(Long id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.update(request.dateTime(), request.notes());
        Appointment saved = appointmentRepository.save(appointment);
        // TODO publish AppointmentEvent(UPDATED)
        return AppointmentResponse.from(saved);
    }

    public AppointmentResponse getById(Long id) {
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