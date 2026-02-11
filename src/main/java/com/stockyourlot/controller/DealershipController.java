package com.stockyourlot.controller;

import com.stockyourlot.dto.CreateDealershipRequest;
import com.stockyourlot.dto.DealershipResponse;
import com.stockyourlot.service.DealershipService;
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

    public DealershipController(DealershipService dealershipService) {
        this.dealershipService = dealershipService;
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

    @GetMapping
    public ResponseEntity<List<DealershipResponse>> getAllDealerships() {
        return ResponseEntity.ok(dealershipService.getAll());
    }
}
