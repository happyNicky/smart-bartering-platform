package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByStatus(Status status);
}
