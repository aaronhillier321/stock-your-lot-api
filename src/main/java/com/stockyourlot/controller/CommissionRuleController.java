package com.stockyourlot.controller;

import com.stockyourlot.dto.CommissionRuleResponse;
import com.stockyourlot.dto.CreateCommissionRuleRequest;
import com.stockyourlot.dto.UpdateCommissionRuleRequest;
import com.stockyourlot.service.CommissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commission-rules")
public class CommissionRuleController {

    private final CommissionService commissionService;

    public CommissionRuleController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    /**
     * Get all commission rules.
     */
    @GetMapping
    public ResponseEntity<List<CommissionRuleResponse>> getAllRules() {
        return ResponseEntity.ok(commissionService.getAllRules());
    }

    /**
     * Create a commission rule (flat amount or percentage).
     */
    @PostMapping
    public ResponseEntity<CommissionRuleResponse> createRule(@Valid @RequestBody CreateCommissionRuleRequest request) {
        CommissionRuleResponse response = commissionService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommissionRuleResponse> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommissionRuleRequest request) {
        return ResponseEntity.ok(commissionService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        commissionService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
