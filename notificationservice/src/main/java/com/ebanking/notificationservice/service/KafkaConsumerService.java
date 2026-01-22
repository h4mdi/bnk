package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.dto.TransactionEvent;
import com.ebanking.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "transaction-events", groupId = "notification-service-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        try {
            log.info("Received transaction event: transactionId={}, type={}, amount={}", 
                event.getTransactionId(), event.getTransactionType(), event.getAmount());
            
            if ("COMPLETED".equals(event.getStatus())) {
                createNotificationForTransaction(event);
            }
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage(), e);
        }
    }
    
    private void createNotificationForTransaction(TransactionEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(Notification.NotificationType.DEFAULT);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setNotificationId("NOTIF-" + event.getTransactionId());
        
        String message = buildNotificationMessage(event);
        notification.setMessage(message);
        
        notification.setSubject("Transaction " + event.getTransactionType());
        
        notificationService.createNotification(notification);
        log.info("Notification created for transaction: {}", event.getTransactionId());
    }
    
    private String buildNotificationMessage(TransactionEvent event) {
        StringBuilder message = new StringBuilder();
        message.append("Transaction ").append(event.getTransactionType()).append(" completed. ");
        message.append("Amount: ").append(event.getAmount()).append(" ").append(event.getCurrency()).append(". ");
        
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            message.append("Description: ").append(event.getDescription()).append(". ");
        }
        
        message.append("Transaction ID: ").append(event.getTransactionId());
        
        return message.toString();
    }
}
