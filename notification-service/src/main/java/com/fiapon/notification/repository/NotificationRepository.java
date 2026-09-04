package com.fiapon.notification.repository;

import com.fiapon.notification.entity.Notification;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, ObjectId> {

    List<Notification> findAllByOrderByScheduledSendAtAsc();

    List<Notification> findByStatusAndScheduledSendAtLessThanEqual(String status, OffsetDateTime scheduledSendAt);

    Optional<Notification> findByAppointmentIdAndStatus(String appointmentId, String status);

}
