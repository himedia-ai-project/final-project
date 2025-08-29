package com.gigigenie.domain.notification.controller;

import com.gigigenie.domain.notification.dto.NotificationDTO;
import com.gigigenie.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @Parameter(description = "회원 ID", required = true)
            @RequestParam Integer memberId) {
        return ResponseEntity.ok(notificationService.getNotifications(memberId));
    }

    @PostMapping
    @Operation(summary = "알림 추가", description = "사용자에게 알림을 추가합니다.")
    public ResponseEntity<?> addNotification(
            @Parameter(description = "알림 정보", required = true)
            @RequestBody Map<String, Object> request) {

        Integer memberId = Integer.valueOf(request.get("memberId").toString());
        String message = (String) request.get("message");
        String title = request.containsKey("title") ? (String) request.get("title") : "";

        notificationService.addNotification(memberId, message, title);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    public ResponseEntity<?> removeNotification(
            @Parameter(description = "알림 ID", required = true)
            @PathVariable Integer id) {
        notificationService.removeNotification(id);
        return ResponseEntity.ok().build();
    }

}
