package com.fiapon.notification.dto;

import org.bson.types.ObjectId;

import java.time.OffsetDateTime;

public record NotificationResponse(
        ObjectId id,
        String appointmentId,
        String patientUsername,
        String patientName,
        String channel,
        String message,
        String appointmentStatus,
        OffsetDateTime scheduledSendAt,
        OffsetDateTime sentAt,
        String status
) {
}
