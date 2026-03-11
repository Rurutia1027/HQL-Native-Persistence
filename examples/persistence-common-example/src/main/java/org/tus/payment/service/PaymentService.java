package org.tus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.persistence.QueryService;
import org.tus.payment.entity.Payment;
import org.tus.payment.entity.PaymentRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final QueryService queryService;

    /**
     * Query User Payment Records
     */
    public List<Payment> getUserPayments(Long userId, Integer paymentStatus) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("p")
                .eq("p.userId", userId)
                .isNull("p.deleted");

        if (paymentStatus != null) {
            builder.and().eq("p.paymentStatus", paymentStatus);
        }

        builder.orderBy("p.createdDate", false);

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        return queryService.query(hql, params);
    }

    /**
     * Query Payment record via payment id
     */
    public Payment getPaymentByPaymentId(String paymentId, Long userId) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("p")
                .eq("p.paymentId", paymentId)
                .and()
                .eq("p.userId", userId)
                .isNull("p.deleted");

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        Object result = queryService.querySingle(hql, params, null);
        return result != null ? (Payment) result : null;
    }

    /**
     * Query Payment record via order id
     */
    public Payment getPaymentByOrderId(String orderId, Long userId) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("p")
                .eq("p.orderId", orderId)
                .and()
                .eq("p.userId", userId)
                .isNull("p.deleted")
                .orderBy("p.createdDate", false);

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Payment> payments = queryService.query(hql, params);
        return payments.isEmpty() ? null : payments.get(0);
    }

    /**
     * Create payment record
     */
    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment.getPaymentId() == null) {
            payment.setPaymentId(generatePaymentId());
        }

        payment.setPaymentStatus(0); // to be paid
        Payment saved = queryService.save(payment);

        // record audit log for the payment
        recordPaymentStatusChange(saved, 1, 0, null, "create payment");

        return saved;
    }

    /**
     * Update payment status
     */
    @Transactional
    public Payment updatePaymentStatus(String paymentId, Long userId, Integer newStatus,
                                       String thirdPartyTradeNo) {
        Payment payment = getPaymentByPaymentId(paymentId, userId);
        if (payment == null) {
            throw new RuntimeException("Payment not found: " + paymentId);
        }

        Integer oldStatus = payment.getPaymentStatus();
        payment.setPaymentStatus(newStatus);

        if (thirdPartyTradeNo != null) {
            payment.setThirdPartyTradeNo(thirdPartyTradeNo);
        }

        if (newStatus == 2) {
            // pay success
            payment.setPayTime(new Date());
        }

        Payment saved = queryService.save(payment);

        // record payment status change
        recordPaymentStatusChange(saved, mapStatusToRecordType(newStatus), oldStatus,
                newStatus, "payment status updated");

        return saved;
    }

    /**
     * Payment Records Status Update
     */
    @Transactional
    public void recordPaymentStatusChange(Payment payment, Integer recordType,
                                          Integer oldStatus, Integer newStatus, String remark) {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId(payment.getPaymentId());
        record.setOrderId(payment.getOrderId());
        record.setUserId(payment.getUserId());
        record.setRecordType(recordType);
        record.setOldStatus(oldStatus);
        record.setNewStatus(newStatus);
        record.setAmount(payment.getPaymentAmount());
        record.setThirdPartyTradeNo(payment.getThirdPartyTradeNo());
        record.setOperatorType(2); // System Operation
        record.setRemark(remark);

        queryService.save(record);
    }

    /**
     * Query Payment Records
     */
    public List<PaymentRecord> getPaymentRecords(String paymentId, Long userId) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(PaymentRecord.class, "pr")
                .select("pr")
                .eq("pr.paymentId", paymentId)
                .eq("pr.userId", userId)
                .isNull("pr.deleted")
                .orderBy("pr.createdDate", true);

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        return queryService.query(hql, params);
    }

    /**
     * Generate Payment ID
     */
    private String generatePaymentId() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0
                , 8).toUpperCase();
    }

    /**
     * Map payment status into String Type
     */
    private Integer mapStatusToRecordType(Integer status) {
        // 0 - Pending Payment
        // 1 - Processing Payment
        // 2 - Payment Successful
        // 3 - Payment Failed
        // 4 - Canceled
        // 5 - Refunded
        switch (status) {
            case 1:
                return 2; // Processing Payment
            case 2:
                return 3; // Payment Successful
            case 3:
                return 4; // Payment Failed
            case 4:
                return 5; // Payment Canceled
            case 5:
                return 6; // Refunded
            default:
                return 1; // Payment Created
        }
    }
}
