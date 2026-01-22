package com.ebanking.accountservice.service;

import com.ebanking.accountservice.model.Account;
import com.ebanking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    
    public Account updateAccount(Long id, Account account) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        existingAccount.setAccountNumber(account.getAccountNumber());
        existingAccount.setAccountType(account.getAccountType());
        existingAccount.setBalance(account.getBalance());
        existingAccount.setUserId(account.getUserId());
        
        return accountRepository.save(existingAccount);
    }
    
    public Account updateBalance(Long id, BigDecimal amount, String transactionType) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        BigDecimal currentBalance = existingAccount.getBalance();
        BigDecimal newBalance;
        
        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            newBalance = currentBalance.add(amount);
        } else if ("WITHDRAWAL".equalsIgnoreCase(transactionType)) {
            newBalance = currentBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
        } else {
            throw new RuntimeException("Invalid transaction type");
        }
        
        existingAccount.setBalance(newBalance);
        return accountRepository.save(existingAccount);
    }
    
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
