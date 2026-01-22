package com.ebanking.transactionservice.client;

import com.ebanking.transactionservice.dto.Account;
import com.ebanking.transactionservice.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "accountservice")
public interface AccountServiceClient {
    
    @GetMapping("/api/accounts/{id}")
    ResponseEntity<Account> getAccountById(@PathVariable Long id);
    
    @PutMapping("/api/accounts/{id}/balance")
    ResponseEntity<Account> updateBalance(@PathVariable Long id, @RequestBody BalanceUpdateRequest request);
}
