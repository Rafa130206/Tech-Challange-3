package com.fiapon.scheduling.service;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.dto.appointment.AppointmentResponse;
import com.fiapon.scheduling.exception.AppointmentNotFoundException;
import com.fiapon.scheduling.exception.InvalidAppointmentStatusTransitionException;
import com.fiapon.scheduling.messaging.AppointmentEventPublisher;
import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.model.AppointmentStatus;
import com.fiapon.scheduling.repository.AppointmentRepository;
import com.fiapon.scheduling.validation.AppointmentValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final List<AppointmentValidator> validators;
    private final AppointmentEventPublisher appointmentEventPublisher;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            List<AppointmentValidator> validators,
            AppointmentEventPublisher appointmentEventPublisher
    ) {
        this.appointmentRepository = appointmentRepository;
        this.validators = validators;
        this.appointmentEventPublisher = appointmentEventPublisher;
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
        appointmentEventPublisher.publishCreated(saved);
        return AppointmentResponse.from(saved);
    }

    public AppointmentResponse update(UUID id, AppointmentRequest request) {
        validators.forEach(v -> v.validate(request, id));
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.update(request.dateTime(), request.notes());
        Appointment saved = appointmentRepository.save(appointment);
        appointmentEventPublisher.publishUpdated(saved);
        return AppointmentResponse.from(saved);
    }

    public AppointmentResponse updateStatus(UUID id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        AppointmentStatus currentStatus = appointment.getStatus();
        if (currentStatus == AppointmentStatus.CANCELLED || currentStatus == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Appointment " + id + " is already " + currentStatus + " and cannot be changed");
        }
        if (newStatus == AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Appointment " + id + " cannot be set back to SCHEDULED");
        }

        appointment.updateStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);
        appointmentEventPublisher.publishUpdated(saved);
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
