package com.gigigenie.domain.notification.service;

import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.notification.dto.NotificationDTO;
import com.gigigenie.domain.notification.entity.Notification;
import com.gigigenie.domain.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void addNotification(String message, String title, Authentication authentication) {
        Member member = findMember(authentication);

        Notification notification = Notification.builder()
            .member(member)
            .message(message)
            .title(title)
            .createdAt(LocalDateTime.now())
            .build();

        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getNotifications(Authentication authentication) {
        Member member = findMember(authentication);
        List<Notification> notifications = notificationRepository.findByMemberIdOrderByCreatedAtDesc(
            member.getMemberId());

        return notifications.stream()
            .map(notification -> NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .title(notification.getTitle())
                .time(notification.getCreatedAt().format(TIME_FORMATTER))
                .build())
            .collect(Collectors.toList());
    }

    public void removeNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private Member findMember(Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        return memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

}
