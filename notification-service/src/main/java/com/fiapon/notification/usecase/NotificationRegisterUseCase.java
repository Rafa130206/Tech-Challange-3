package com.fiapon.notification.usecase;

import com.fiapon.notification.entity.Notification;
import com.fiapon.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class NotificationRegisterUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationRegisterUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification registerNotification(
            UUID appointmentId,
            String patientUsername,
            String patientName,
            String doctorName,
            String scheduledAt,
            String appointmentStatus
    ) {
        OffsetDateTime appointmentDate = OffsetDateTime.parse(scheduledAt);
        OffsetDateTime reminderDate = appointmentDate.minusDays(1);
        Notification notification = new Notification(
                null,
                "PENDENTE",
                null,
                reminderDate,
                appointmentStatus,
                "Lembrete: consulta com " + doctorName + " em " + scheduledAt,
                "EMAIL",
                patientName,
                patientUsername,
                appointmentId.toString()
        );
        return notificationRepository.save(notification);
    }
}
