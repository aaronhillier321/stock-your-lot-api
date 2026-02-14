package com.stockyourlot.controller;

import com.stockyourlot.dto.CreateDealershipRequest;
import com.stockyourlot.dto.DealershipResponse;
import com.stockyourlot.dto.UpdateDealershipRequest;
import com.stockyourlot.dto.PurchaseResponse;
import com.stockyourlot.service.DealershipService;
import com.stockyourlot.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dealerships")
public class DealershipController {

    private final DealershipService dealershipService;
    private final PurchaseService purchaseService;

    public DealershipController(DealershipService dealershipService, PurchaseService purchaseService) {
        this.dealershipService = dealershipService;
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<DealershipResponse> createDealership(
            @Valid @RequestBody CreateDealershipRequest request) {
        DealershipResponse response = dealershipService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealershipResponse> getDealershipById(@PathVariable UUID id) {
        return ResponseEntity.ok(dealershipService.getById(id));
    }

    /**
     * Update a dealership by ID. Only provided fields in the body are updated.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DealershipResponse> updateDealership(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDealershipRequest request) {
        return ResponseEntity.ok(dealershipService.update(id, request));
    }

    @GetMapping("/{id}/purchases")
    public ResponseEntity<List<PurchaseResponse>> getDealershipPurchases(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.getByDealershipId(id));
    }

    @GetMapping
    public ResponseEntity<List<DealershipResponse>> getAllDealerships() {
        return ResponseEntity.ok(dealershipService.getAll());
    }
}
