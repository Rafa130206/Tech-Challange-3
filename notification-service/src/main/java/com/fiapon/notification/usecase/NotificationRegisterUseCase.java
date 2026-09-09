package com.fiapon.notification.usecase;

import com.fiapon.notification.entity.Notification;
import com.fiapon.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class NotificationRegisterUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationRegisterUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification registerNotification(
            String appointmentId,
            String patientUsername,
            String patientName,
            String doctorName,
            String scheduledAt,
            String appointmentStatus
    ) {
        // If this appointment already has a pending reminder (e.g. it was rescheduled),
        // reuse that document's id so save() updates it in place instead of creating
        // a duplicate reminder with the stale date/time.
        Optional<Notification> pendingReminder = notificationRepository
                .findByAppointmentIdAndStatus(appointmentId, "PENDENTE");

        // A cancelled appointment has no reminder to send anymore: cancel the pending
        // one in place instead of refreshing it with the (now meaningless) date/time.
        if ("CANCELLED".equalsIgnoreCase(appointmentStatus)) {
            return pendingReminder.map(existing -> cancelReminder(existing, appointmentStatus)).orElse(null);
        }

        OffsetDateTime appointmentDate = OffsetDateTime.parse(scheduledAt);
        OffsetDateTime reminderDate = appointmentDate.minusDays(1);

        Notification notification = new Notification(
                pendingReminder.map(Notification::getId).orElse(null),
                "PENDENTE",
                null,
                reminderDate,
                appointmentStatus,
                "Lembrete: consulta com " + doctorName + " em " + scheduledAt,
                "EMAIL",
                patientName,
                patientUsername,
                appointmentId
        );
        return notificationRepository.save(notification);
    }

    private Notification cancelReminder(Notification existing, String appointmentStatus) {
        Notification cancelled = new Notification(
                existing.getId(),
                "CANCELADA",
                null,
                existing.getScheduledSendAt(),
                appointmentStatus,
                "Consulta cancelada",
                existing.getChannel(),
                existing.getPatientName(),
                existing.getPatientUsername(),
                existing.getAppointmentId()
        );
        return notificationRepository.save(cancelled);
    }
}
