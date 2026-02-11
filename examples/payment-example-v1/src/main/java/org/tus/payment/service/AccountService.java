package org.tus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.persistence.QueryService;
import org.tus.payment.entity.AccountBalance;
import org.tus.payment.entity.AccountTransaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/***
 * Account Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final QueryService queryService;

    /**
     * Query account balance
     */
    public AccountBalance getAccountBalance(Long userId) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(AccountBalance.class, "ab")
                .select("ab")
                .eq("ab.userId", userId)
                .isNull("ab.deleted");

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        Object result = queryService.querySingle(hql, params, null);
        return result != null ? (AccountBalance) result : null;
    }

    /**
     * Create or Fetch Account Balance
     */
    @Transactional
    public AccountBalance getOrCreateAccountBalance(Long userId) {
        AccountBalance balance = getAccountBalance(userId);
        if (balance == null) {
            balance = new AccountBalance();
            balance.setUserId(userId);
            balance.setAvailableBalance(BigDecimal.ZERO);
            balance.setFrozenBalance(BigDecimal.ZERO);
            balance.setTotalBalance(BigDecimal.ZERO);
            balance.setAccountStatus(0); // Active
            balance = queryService.save(balance);
        }
        return balance;
    }

    /**
     * Top-up
     */
    @Transactional
    public AccountTransaction recharge(Long userId, BigDecimal amount, String remark) {
        AccountBalance balance = getOrCreateAccountBalance(userId);

        BigDecimal balanceBefore = balance.getAvailableBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        balance.setAvailableBalance(balanceAfter);
        balance.setTotalBalance(balance.getAvailableBalance().add(balance.getFrozenBalance()));
        balance.setLastTransactionTime(new Date());
        queryService.save(balance);

        // Create and Save Account Transaction
        AccountTransaction transaction = createTransaction(
                userId, 1, amount, balanceBefore, balanceAfter, null, null, null, remark
        );

        return transaction;
    }

    /**
     * Charge
     */
    public AccountTransaction consume(Long userId, BigDecimal amount, String orderId,
                                      String paymentId, String remark) {
        AccountBalance balance = getOrCreateAccountBalance(userId);

        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal balanceBefore = balance.getAvailableBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        balance.setAvailableBalance(balanceAfter);
        balance.setTotalBalance(balance.getAvailableBalance().add(balance.getFrozenBalance()));
        balance.setLastTransactionTime(new Date());
        queryService.save(balance);

        // Record transaction
        AccountTransaction transaction = createTransaction(
                userId, 2, amount.negate(), balanceBefore, balanceAfter, orderId, paymentId,
                null, remark);

        return transaction;
    }

    /**
     * Query Account Transaction
     */
    public List<AccountTransaction> getAccountTransaction(Long userId,
                                                          Integer transactionType,
                                                          Integer limit) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(AccountTransaction.class, "at")
                .select("at")
                .eq("at.userId", userId)
                .isNull("at.deleted");

        if (transactionType != null) {
            builder.and().eq("at.transactionType", transactionType);
        }

        builder.orderBy("at.createdData", false);

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<AccountTransaction> transactions = queryService.query(hql, params);

        if (limit != null && limit > 0 && transactions.size() > limit) {
            return transactions.subList(0, limit);
        }
        return transactions;
    }

    /**
     * Create Account Transaction
     */
    @Transactional
    AccountTransaction createTransaction(Long userId, Integer transactionType, BigDecimal amount,
                                         BigDecimal balanceBefore, BigDecimal balanceAfter,
                                         String orderId, String paymentId, String refundId, String remark) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setOrderId(orderId);
        transaction.setPaymentId(paymentId);
        transaction.setRefundId(refundId);
        transaction.setTransactionStatus(1); // Success
        transaction.setRemark(remark);

        return queryService.save(transaction);
    }

    /**
     * Generate transaction ID
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0
                , 8).toUpperCase();
    }
}
