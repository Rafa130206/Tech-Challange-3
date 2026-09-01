package com.fiapon.notification.usecase;

import com.fiapon.notification.entity.Notification;
import com.fiapon.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationListUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationListUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> list() {
        return notificationRepository.findAllByOrderByScheduledSendAtAsc();
    }
}
