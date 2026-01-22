package com.ebanking.accountservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BalanceUpdateRequest {
    private BigDecimal amount;
    private TransactionType transactionType;
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }
}
