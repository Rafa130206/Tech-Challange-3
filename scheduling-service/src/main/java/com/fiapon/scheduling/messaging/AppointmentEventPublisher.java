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
import java.util.UUID;

@Component
public class AppointmentEventPublisher {

    public static final String CREATED_TOPIC = "agendamento.criado";
    public static final String UPDATED_TOPIC = "agendamento.atualizado";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public AppointmentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            UserRepository userRepository
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public void publishCreated(Appointment appointment) {
        publish(CREATED_TOPIC, appointment);
    }

    public void publishUpdated(Appointment appointment) {
        publish(UPDATED_TOPIC, appointment);
    }

    private void publish(String topic, Appointment appointment) {
        User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
        User doctor = userRepository.findById(appointment.getDoctorId()).orElse(null);

        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", topic,
                "appointmentId", appointment.getId(),
                "patientUsername", patient != null ? patient.getUsername() : "patient-" + appointment.getPatientId(),
                "patientName", patient != null ? patient.getName() : "patient-" + appointment.getPatientId(),
                "doctorName", doctor != null ? doctor.getName() : "doctor-" + appointment.getDoctorId(),
                "scheduledAt", appointment.getDateTime().atOffset(ZoneOffset.UTC).toString(),
                "status", appointment.getStatus().name()
        );

        try {
            kafkaTemplate.send(topic, appointment.getId().toString(), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel publicar evento de agendamento", exception);
        }
    }
}
