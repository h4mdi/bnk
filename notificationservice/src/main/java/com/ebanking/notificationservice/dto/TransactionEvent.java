package com.ebanking.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private Long fromAccountId;
    private Long toAccountId;
    private Long userId;
    private String status;
}
