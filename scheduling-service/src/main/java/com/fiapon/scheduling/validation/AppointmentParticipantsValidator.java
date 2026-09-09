package com.fiapon.scheduling.validation;

import com.fiapon.scheduling.dto.appointment.AppointmentRequest;
import com.fiapon.scheduling.exception.InvalidAppointmentParticipantException;
import com.fiapon.scheduling.model.User;
import com.fiapon.scheduling.model.UserRole;
import com.fiapon.scheduling.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Nothing checked that patientId/doctorId in the request actually correspond to real users,
// let alone ones with the right role — an appointment could be booked with a made-up id, or
// with patient/doctor swapped, and it would be silently accepted.
@Component
public class AppointmentParticipantsValidator implements AppointmentValidator {

    private final UserRepository userRepository;

    public AppointmentParticipantsValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validate(AppointmentRequest request, UUID appointmentIdBeingUpdated) {
        User patient = userRepository.findById(request.patientId())
                .orElseThrow(() -> new InvalidAppointmentParticipantException(
                        "patientId " + request.patientId() + " does not correspond to any registered user"));
        if (patient.getRole() != UserRole.PATIENT) {
            throw new InvalidAppointmentParticipantException(
                    "patientId " + request.patientId() + " belongs to a " + patient.getRole() + ", not a PATIENT");
        }

        User doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new InvalidAppointmentParticipantException(
                        "doctorId " + request.doctorId() + " does not correspond to any registered user"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new InvalidAppointmentParticipantException(
                    "doctorId " + request.doctorId() + " belongs to a " + doctor.getRole() + ", not a DOCTOR");
        }
    }
}
