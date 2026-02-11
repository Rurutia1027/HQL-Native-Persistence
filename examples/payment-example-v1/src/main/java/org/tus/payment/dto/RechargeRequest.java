package org.tus.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Recharge Request DTO
 */
@Data
public class RechargeRequest {
    private Long userId;
    private BigDecimal amount;
    private String remark;
}
