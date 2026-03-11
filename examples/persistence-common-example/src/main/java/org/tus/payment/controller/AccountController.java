package org.tus.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tus.payment.dto.ApiResponse;
import org.tus.payment.dto.RechargeRequest;
import org.tus.payment.entity.AccountBalance;
import org.tus.payment.entity.AccountTransaction;
import org.tus.payment.service.AccountService;

import java.util.List;

/**
 * Account Controller - REST API for Account operations
 */
@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    @Autowired
    private AccountService accountService;
    
    /**
     * Get account balance
     * GET /api/accounts/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<AccountBalance>> getAccountBalance(@PathVariable Long userId) {
        try {
            AccountBalance balance = accountService.getAccountBalance(userId);
            if (balance == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account balance not found", 404));
            }
            return ResponseEntity.ok(ApiResponse.success(balance));
        } catch (Exception e) {
            log.error("Error getting account balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get account balance: " + e.getMessage()));
        }
    }
    
    /**
     * Recharge account
     * POST /api/accounts/recharge
     */
    @PostMapping("/recharge")
    public ResponseEntity<ApiResponse<AccountTransaction>> recharge(@RequestBody RechargeRequest request) {
        try {
            AccountTransaction transaction = accountService.recharge(
                    request.getUserId(),
                    request.getAmount(),
                    request.getRemark()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Recharge successful", transaction));
        } catch (Exception e) {
            log.error("Error recharging account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to recharge: " + e.getMessage()));
        }
    }
    
    /**
     * Get account transactions
     * GET /api/accounts/{userId}/transactions?type={type}&limit={limit}
     */
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<AccountTransaction>>> getAccountTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer limit) {
        try {
            List<AccountTransaction> transactions = accountService.getAccountTransactions(userId, type, limit);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            log.error("Error getting account transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get transactions: " + e.getMessage()));
        }
    }
}
