package com.fiapon.notification.usecase;

import com.fiapon.notification.entity.Notification;
import com.fiapon.notification.messaging.NotificationEventPublisher;
import com.fiapon.notification.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class NotificationScheduleUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public NotificationScheduleUseCase(NotificationRepository notificationRepository, NotificationEventPublisher notificationEventPublisher) {
        this.notificationRepository = notificationRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Scheduled(fixedDelayString = "${app.notification.dispatch-interval-ms}")
    public void dispatchPendingNotifications() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Notification> pendingNotifications = notificationRepository.findByStatusAndScheduledSendAtLessThanEqual("PENDENTE", now);

        for (Notification notification : pendingNotifications) {
            Notification sentNotification = new Notification(
                    notification.getId(),
                    "ENVIADA",
                    now,
                    notification.getScheduledSendAt(),
                    notification.getAppointmentStatus(),
                    notification.getMessage(),
                    notification.getChannel(),
                    notification.getPatientName(),
                    notification.getPatientUsername(),
                    notification.getAppointmentId()
            );
            Notification savedNotification = notificationRepository.save(sentNotification);
            notificationEventPublisher.publishSent(savedNotification);
        }
    }
}
