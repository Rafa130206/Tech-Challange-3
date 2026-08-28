package com.fiapon.notification.controller;

import com.fiapon.notification.dto.NotificationResponse;
import com.fiapon.notification.mapper.NotificationMapper;
import com.fiapon.notification.usecase.NotificationListUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificationController {

    private final NotificationListUseCase notificationListUseCase;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationListUseCase notificationListUseCase, NotificationMapper notificationMapper) {
        this.notificationListUseCase = notificationListUseCase;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        return notificationListUseCase.list()
                .stream()
                .map(NotificationMapper::toRespose)
                .toList();
    }
}
