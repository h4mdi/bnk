package com.ebanking.transactionservice.service;

import com.ebanking.transactionservice.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    
    private static final String TOPIC = "transaction-events";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    
    public void sendTransactionEvent(TransactionEvent event) {
        try {
            CompletableFuture<SendResult<String, TransactionEvent>> future = 
                kafkaTemplate.send(TOPIC, event.getTransactionId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Transaction event sent successfully: transactionId={}, offset={}", 
                        event.getTransactionId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send transaction event: transactionId={}, error={}", 
                        event.getTransactionId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error sending transaction event: {}", e.getMessage(), e);
        }
    }
}
