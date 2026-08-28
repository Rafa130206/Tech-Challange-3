package com.fiapon.notification.mapper;

import com.fiapon.notification.dto.NotificationResponse;
import com.fiapon.notification.entity.Notification;

public class NotificationMapper {

    public static NotificationResponse toRespose(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAppointmentId(),
                notification.getPatientUsername(),
                notification.getPatientName(),
                notification.getChannel(),
                notification.getMessage(),
                notification.getAppointmentStatus(),
                notification.getScheduledSendAt(),
                notification.getSentAt(),
                notification.getStatus()
        );
    }
}
