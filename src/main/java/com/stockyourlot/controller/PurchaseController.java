package com.stockyourlot.controller;

import com.stockyourlot.dto.CreatePurchaseRequest;
import com.stockyourlot.dto.PurchaseResponse;
import com.stockyourlot.dto.UpdatePurchaseRequest;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Get all purchases (all buyers). Optional date range: startDate and/or endDate (inclusive).
     * If both omitted, returns all purchases. Requires authentication.
     */
    @GetMapping
    public ResponseEntity<List<PurchaseResponse>> getAll(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(purchaseService.getAll(startDate, endDate));
    }

    /**
     * Get a single purchase by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.getById(id));
    }

    /**
     * Get purchases for the currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<List<PurchaseResponse>> getMyPurchases(@AuthenticationPrincipal Object principal) {
        if (!(principal instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(purchaseService.getByBuyerId(user.getId()));
    }

    /**
     * Get purchases for a specific buyer by ID.
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<PurchaseResponse>> getByBuyerId(@PathVariable UUID buyerId) {
        return ResponseEntity.ok(purchaseService.getByBuyerId(buyerId));
    }

    /**
     * Create a purchase. buyer_id is set to the currently logged-in user.
     */
    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request,
            @AuthenticationPrincipal Object principal) {
        if (!(principal instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PurchaseResponse response = purchaseService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a purchase by ID. Only provided fields in the body are updated.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.update(id, request));
    }
}
