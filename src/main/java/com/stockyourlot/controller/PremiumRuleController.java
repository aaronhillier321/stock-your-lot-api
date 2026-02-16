package com.stockyourlot.controller;

import com.stockyourlot.dto.CreatePremiumRuleRequest;
import com.stockyourlot.dto.PremiumRuleResponse;
import com.stockyourlot.service.PremiumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premium-rules")
public class PremiumRuleController {

    private final PremiumService premiumService;

    public PremiumRuleController(PremiumService premiumService) {
        this.premiumService = premiumService;
    }

    @GetMapping
    public ResponseEntity<List<PremiumRuleResponse>> getAllRules() {
        return ResponseEntity.ok(premiumService.getAllRules());
    }

    @PostMapping
    public ResponseEntity<PremiumRuleResponse> createRule(@Valid @RequestBody CreatePremiumRuleRequest request) {
        PremiumRuleResponse response = premiumService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
