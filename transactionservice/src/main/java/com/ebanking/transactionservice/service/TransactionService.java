package com.ebanking.transactionservice.service;

import com.ebanking.transactionservice.client.AccountServiceClient;
import com.ebanking.transactionservice.dto.Account;
import com.ebanking.transactionservice.dto.BalanceUpdateRequest;
import com.ebanking.transactionservice.dto.TransactionEvent;
import com.ebanking.transactionservice.model.Transaction;
import com.ebanking.transactionservice.repository.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaProducerService kafkaProducerService;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        List<Transaction> fromAccount = transactionRepository.findByFromAccountId(accountId);
        List<Transaction> toAccount = transactionRepository.findByToAccountId(accountId);
        
        fromAccount.addAll(toAccount);
        return fromAccount;
    }
    
    public Transaction createTransaction(Transaction transaction) {
        try {
            validateAccounts(transaction);
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            updateAccountBalances(savedTransaction);
            
            savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction completedTransaction = transactionRepository.save(savedTransaction);
            
            sendTransactionEvent(completedTransaction);
            
            return completedTransaction;
            
        } catch (DataIntegrityViolationException e) {
            log.error("Transaction ID already exists: {}", transaction.getTransactionId());
            throw new RuntimeException("Transaction ID already exists: " + transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Error creating transaction: {}", e.getMessage());
            try {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);
            } catch (Exception saveException) {
                log.error("Failed to save transaction with FAILED status: {}", saveException.getMessage());
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage());
        }
    }
    
    private void validateAccounts(Transaction transaction) {
        try {
            ResponseEntity<Account> fromAccountResponse = accountServiceClient.getAccountById(transaction.getFromAccountId());
            if (fromAccountResponse.getBody() == null) {
                throw new RuntimeException("From account not found: " + transaction.getFromAccountId());
            }
            
            if (transaction.getToAccountId() != null) {
                ResponseEntity<Account> toAccountResponse = accountServiceClient.getAccountById(transaction.getToAccountId());
                if (toAccountResponse.getBody() == null) {
                    throw new RuntimeException("To account not found: " + transaction.getToAccountId());
                }
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Account not found", e);
        } catch (FeignException e) {
            log.error("Error validating accounts: {}", e.getMessage());
            throw new RuntimeException("Error validating accounts: " + e.getMessage(), e);
        }
    }
    
    private void updateAccountBalances(Transaction transaction) {
        try {
            BalanceUpdateRequest request = new BalanceUpdateRequest();
            request.setAmount(transaction.getAmount());
            
            switch (transaction.getTransactionType()) {
                case DEPOSIT:
                    request.setTransactionType(BalanceUpdateRequest.TransactionType.DEPOSIT);
                    Long depositAccountId = transaction.getToAccountId() != null 
                        ? transaction.getToAccountId() 
                        : transaction.getFromAccountId();
                    accountServiceClient.updateBalance(depositAccountId, request);
                    break;
                    
                case WITHDRAWAL:
                    request.setTransactionType(BalanceUpdateRequest.TransactionType.WITHDRAWAL);
                    accountServiceClient.updateBalance(transaction.getFromAccountId(), request);
                    break;
                    
                case TRANSFER:
                    if (transaction.getToAccountId() == null) {
                        throw new RuntimeException("To account is required for TRANSFER");
                    }
                    BalanceUpdateRequest withdrawalRequest = new BalanceUpdateRequest();
                    withdrawalRequest.setAmount(transaction.getAmount());
                    withdrawalRequest.setTransactionType(BalanceUpdateRequest.TransactionType.WITHDRAWAL);
                    accountServiceClient.updateBalance(transaction.getFromAccountId(), withdrawalRequest);
                    
                    BalanceUpdateRequest depositRequest = new BalanceUpdateRequest();
                    depositRequest.setAmount(transaction.getAmount());
                    depositRequest.setTransactionType(BalanceUpdateRequest.TransactionType.DEPOSIT);
                    accountServiceClient.updateBalance(transaction.getToAccountId(), depositRequest);
                    break;
            }
        } catch (FeignException.BadRequest e) {
            log.error("Bad request updating balance: {}", e.getMessage());
            throw new RuntimeException("Insufficient balance or invalid request", e);
        } catch (FeignException e) {
            log.error("Error updating account balances: {}", e.getMessage());
            throw new RuntimeException("Error updating account balances: " + e.getMessage(), e);
        }
    }
    
    private void sendTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = new TransactionEvent();
            event.setTransactionId(transaction.getTransactionId());
            event.setTransactionType(transaction.getTransactionType().name());
            event.setAmount(transaction.getAmount());
            event.setCurrency(transaction.getCurrency());
            event.setDescription(transaction.getDescription());
            event.setFromAccountId(transaction.getFromAccountId());
            event.setToAccountId(transaction.getToAccountId());
            event.setUserId(transaction.getUserId());
            event.setStatus(transaction.getStatus().name());
            
            kafkaProducerService.sendTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error sending transaction event: {}", e.getMessage());
        }
    }
    
    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        existingTransaction.setTransactionId(transaction.getTransactionId());
        existingTransaction.setTransactionType(transaction.getTransactionType());
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setFromAccountId(transaction.getFromAccountId());
        existingTransaction.setToAccountId(transaction.getToAccountId());
        existingTransaction.setUserId(transaction.getUserId());
        
        return transactionRepository.save(existingTransaction);
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
