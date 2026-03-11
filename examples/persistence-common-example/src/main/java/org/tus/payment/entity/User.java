package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User Entity - user table
 * <p>
 * Sharding Key: user_id
 */
@Entity
@Table(name = "t_users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends PersistedObject {
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId; // Sharding Key

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(name = "id_card", length = 32)
    private String idCard;

    // 0 - Active
    // 1 - Frozen
    // 2 - Closed
    @Column(name = "account_status")
    private Integer accountStatus;

    @Column(name = "account_balance", precision = 18, scale = 2)
    private BigDecimal accountBalance;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "gender")
    private Integer gender; // 0-Unknown，1-Male，2-Female

    @Temporal(TemporalType.DATE)
    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // JSON format
}
