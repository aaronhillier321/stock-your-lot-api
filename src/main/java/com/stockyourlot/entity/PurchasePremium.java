package com.stockyourlot.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Premium (service fee) amount applied to a purchase for the dealership.
 * One row per purchase; amount is stored flat (percentage rules converted at sale time).
 */
@Entity
@Table(name = "purchase_premium", indexes = {
        @Index(name = "idx_purchase_premium_purchase_id", columnList = "purchase_id"),
        @Index(name = "idx_purchase_premium_dealership_id", columnList = "dealership_id"),
        @Index(name = "idx_purchase_premium_rule_id", columnList = "rule_id")
})
public class PurchasePremium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealership_id", nullable = false)
    private Dealership dealership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private PremiumRule rule;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onPersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public PurchasePremium() {}

    public PurchasePremium(Purchase purchase, Dealership dealership, PremiumRule rule, BigDecimal amount) {
        this.purchase = purchase;
        this.dealership = dealership;
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

    public Dealership getDealership() {
        return dealership;
    }

    public void setDealership(Dealership dealership) {
        this.dealership = dealership;
    }

    public PremiumRule getRule() {
        return rule;
    }

    public void setRule(PremiumRule rule) {
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
