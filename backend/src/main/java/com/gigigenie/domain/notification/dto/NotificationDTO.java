package com.gigigenie.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDTO {
    private Integer id;
    private String message;
    private String title;
    private String time;
}
