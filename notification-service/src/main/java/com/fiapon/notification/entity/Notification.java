package com.fiapon.notification.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(collection = "notifications")
public class Notification {

    @Id
    private ObjectId id;
    private String appointmentId;
    private String patientUsername;
    private String patientName;
    private String channel;
    private String message;
    private String appointmentStatus;
    private OffsetDateTime scheduledSendAt;
    private OffsetDateTime sentAt;
    private String status;

    public Notification() {
    }

    public Notification(ObjectId id, String status, OffsetDateTime sentAt, OffsetDateTime scheduledSendAt, String appointmentStatus, String message, String channel, String patientName, String patientUsername, String appointmentId) {
        this.id = id;
        this.status = status;
        this.sentAt = sentAt;
        this.scheduledSendAt = scheduledSendAt;
        this.appointmentStatus = appointmentStatus;
        this.message = message;
        this.channel = channel;
        this.patientName = patientName;
        this.patientUsername = patientUsername;
        this.appointmentId = appointmentId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public OffsetDateTime getScheduledSendAt() {
        return scheduledSendAt;
    }

    public void setScheduledSendAt(OffsetDateTime scheduledSendAt) {
        this.scheduledSendAt = scheduledSendAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
