package com.fiapon.scheduling.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.model.User;
import com.fiapon.scheduling.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Map;

@Component
public class AppointmentEventPublisher {

    private static final String TOPIC_CREATED = "agendamento.criado";
    private static final String TOPIC_UPDATED = "agendamento.atualizado";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppointmentEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     UserRepository userRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
    }

    public void publishCreated(Appointment appointment) {
        publish(TOPIC_CREATED, appointment);
    }

    public void publishUpdated(Appointment appointment) {
        publish(TOPIC_UPDATED, appointment);
    }

    private void publish(String topic, Appointment appointment) {
        User patient = userRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + appointment.getPatientId()));
        User doctor = userRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new IllegalStateException("Doctor not found: " + appointment.getDoctorId()));

        Map<String, Object> payload = Map.of(
                "appointmentId", appointment.getId().toString(),
                "patientUsername", patient.getUsername(),
                "patientName", patient.getName(),
                "doctorName", doctor.getName(),
                "scheduledAt", appointment.getDateTime().atOffset(ZoneOffset.UTC).toString(),
                "status", appointment.getStatus().name()
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, appointment.getId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize appointment event", e);
        }
    }
}