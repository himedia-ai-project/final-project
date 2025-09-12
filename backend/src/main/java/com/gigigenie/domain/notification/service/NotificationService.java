package com.gigigenie.domain.notification.service;

import com.gigigenie.domain.notification.dto.NotificationDTO;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface NotificationService {

    void addNotification(String message, String title, Authentication authentication);

    List<NotificationDTO> getNotifications(Authentication authentication);

    void removeNotification(Integer notificationId);

}
