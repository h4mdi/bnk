package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    public Notification createNotification(Notification notification) {
        if (notification.getType() == null) {
            notification.setType(Notification.NotificationType.DEFAULT);
        }
        
        if (notification.getNotificationType() != null) {
            notification.setType(notification.getNotificationType());
        }
        
        return notificationRepository.save(notification);
    }
    
    public Notification updateNotification(Long id, Notification notification) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (notification.getUserId() != null) {
            existingNotification.setUserId(notification.getUserId());
        }
        
        if (notification.getMessage() != null) {
            existingNotification.setMessage(notification.getMessage());
        }
        
        if (notification.getType() != null) {
            existingNotification.setType(notification.getType());
        }
        
        if (notification.getNotificationType() != null) {
            existingNotification.setNotificationType(notification.getNotificationType());
        }
        
        if (notification.getStatus() != null) {
            existingNotification.setStatus(notification.getStatus());
        }
        
        if (notification.getNotificationId() != null) {
            existingNotification.setNotificationId(notification.getNotificationId());
        }
        
        if (notification.getRecipient() != null) {
            existingNotification.setRecipient(notification.getRecipient());
        }
        
        if (notification.getSubject() != null) {
            existingNotification.setSubject(notification.getSubject());
        }
        
        return notificationRepository.save(existingNotification);
    }
    
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
