package com.fiapon.notification.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapon.notification.usecase.NotificationRegisterUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class AppointmentEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRegisterUseCase notificationRegisterUseCase;

    public AppointmentEventConsumer(ObjectMapper objectMapper, NotificationRegisterUseCase notificationRegisterUseCase) {
        this.objectMapper = objectMapper;
        this.notificationRegisterUseCase = notificationRegisterUseCase;
    }

    @KafkaListener(topics = {"agendamento.criado", "agendamento.atualizado"}, groupId = "${app.kafka.groups.notificacao}")
    public void consume(String message) throws IOException {
        JsonNode payload = objectMapper.readTree(message);
        notificationRegisterUseCase.registerNotification(
                UUID.fromString(payload.get("appointmentId").asText()),
                payload.get("patientUsername").asText(),
                payload.get("patientName").asText(),
                payload.get("doctorName").asText(),
                payload.get("scheduledAt").asText(),
                payload.get("status").asText()
        );
    }
}
