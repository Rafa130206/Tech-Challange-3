package com.fiapon.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapon.notification.entity.Notification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventPublisher {

    public static final String TOPIC = "notificacao.enviada";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSent(Notification notification) {
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("eventId", UUID.randomUUID().toString()),
                Map.entry("eventType", TOPIC),
                Map.entry("notificationId", notification.getId().toString()),
                Map.entry("appointmentId", notification.getAppointmentId().toString()),
                Map.entry("patientUsername", notification.getPatientUsername()),
                Map.entry("patientName", notification.getPatientName()),
                Map.entry("channel", notification.getChannel()),
                Map.entry("message", notification.getMessage()),
                Map.entry("appointmentStatus", notification.getAppointmentStatus()),
                Map.entry("scheduledSendAt", notification.getScheduledSendAt().toString()),
                Map.entry("sentAt", notification.getSentAt().toString()),
                Map.entry("status", notification.getStatus()),
                Map.entry("publishedAt", OffsetDateTime.now().toString())
        );

        try {
            kafkaTemplate.send(TOPIC, notification.getAppointmentId().toString(), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel publicar evento de notificacao", exception);
        }
    }
}
