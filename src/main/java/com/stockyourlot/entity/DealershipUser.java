package com.stockyourlot.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dealership_users", indexes = {
        @Index(name = "idx_dealership_users_user_id", columnList = "user_id"),
        @Index(name = "idx_dealership_users_dealership_id", columnList = "dealership_id")
})
public class DealershipUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dealership_id", nullable = false)
    private Dealership dealership;

    @Column(name = "role", nullable = false, length = 20)
    private String dealershipRole = "BUYER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onPersist() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    protected DealershipUser() {}

    public DealershipUser(User user, Dealership dealership, String dealershipRole) {
        this.user = user;
        this.dealership = dealership;
        this.dealershipRole = (dealershipRole != null && (dealershipRole.equals("ADMIN") || dealershipRole.equals("BUYER")))
                ? dealershipRole : "BUYER";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Dealership getDealership() {
        return dealership;
    }

    public void setDealership(Dealership dealership) {
        this.dealership = dealership;
    }

    public String getDealershipRole() {
        return dealershipRole;
    }

    public void setDealershipRole(String dealershipRole) {
        if (dealershipRole != null && (dealershipRole.equals("ADMIN") || dealershipRole.equals("BUYER"))) {
            this.dealershipRole = dealershipRole;
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
