package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByStatus(Status status);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.emailNotificationId = :id AND n.userId = :userId")
    void markAsReadByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.emailNotificationId = :id AND n.userId = :userId")
    void deleteByEmailNotificationIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
