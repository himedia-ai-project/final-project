package com.gigigenie.domain.notification.repository;

import com.gigigenie.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n WHERE n.member.memberId = :memberId ORDER BY n.createdAt DESC")
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Integer memberId);

    @Query("DELETE FROM Notification n WHERE n.member.memberId = :memberId")
    void deleteByMemberId(Integer memberId);
}
