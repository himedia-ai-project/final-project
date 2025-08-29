package com.gigigenie.domain.notification.service;

import com.gigigenie.domain.notification.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    void addNotification(Integer memberId, String message, String title);

    List<NotificationDTO> getNotifications(Integer memberId);

    void removeNotification(Integer notificationId);

}
