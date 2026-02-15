package com.stockyourlot.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Commission amount applied to a purchase for a specific user. One purchase can have
 * many records (multiple users credited). Amount is always stored flat (percentage
 * rules are converted at sale time). rule_id may be null if the rule was later deleted.
 */
@Entity
@Table(name = "purchase_commission", indexes = {
        @Index(name = "idx_purchase_commission_purchase_id", columnList = "purchase_id"),
        @Index(name = "idx_purchase_commission_user_id", columnList = "user_id"),
        @Index(name = "idx_purchase_commission_rule_id", columnList = "rule_id")
})
public class PurchaseCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private CommissionRule rule;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onPersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public PurchaseCommission() {}

    public PurchaseCommission(Purchase purchase, User user, CommissionRule rule, BigDecimal amount) {
        this.purchase = purchase;
        this.user = user;
        this.rule = rule;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CommissionRule getRule() {
        return rule;
    }

    public void setRule(CommissionRule rule) {
        this.rule = rule;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
