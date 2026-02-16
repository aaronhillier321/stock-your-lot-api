package com.stockyourlot.service;

import com.stockyourlot.dto.DealerPremiumRuleInput;
import com.stockyourlot.dto.PremiumRuleResponse;
import com.stockyourlot.dto.CreatePremiumRuleRequest;
import com.stockyourlot.entity.*;
import com.stockyourlot.repository.DealerPremiumRepository;
import com.stockyourlot.repository.PremiumRuleRepository;
import com.stockyourlot.repository.PurchasePremiumRepository;
import com.stockyourlot.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Premium (service fee) rules and dealership assignments. Mirrors CommissionService.
 */
@Service
public class PremiumService {

    private static final Logger log = LoggerFactory.getLogger(PremiumService.class);

    private final PremiumRuleRepository premiumRuleRepository;
    private final DealerPremiumRepository dealerPremiumRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePremiumRepository purchasePremiumRepository;

    public PremiumService(PremiumRuleRepository premiumRuleRepository,
                          DealerPremiumRepository dealerPremiumRepository,
                          PurchaseRepository purchaseRepository,
                          PurchasePremiumRepository purchasePremiumRepository) {
        this.premiumRuleRepository = premiumRuleRepository;
        this.dealerPremiumRepository = dealerPremiumRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchasePremiumRepository = purchasePremiumRepository;
    }

    @Transactional(readOnly = true)
    public List<PremiumRuleResponse> getAllRules() {
        return premiumRuleRepository.findAll().stream()
                .map(r -> new PremiumRuleResponse(r.getId(), r.getRuleName(), r.getAmount(), r.getPremiumType()))
                .toList();
    }

    @Transactional
    public PremiumRuleResponse createRule(CreatePremiumRuleRequest request) {
        PremiumRule rule = new PremiumRule();
        rule.setRuleName(request.ruleName() != null ? request.ruleName().trim() : "");
        rule.setAmount(request.amount());
        rule.setPremiumType(request.premiumType());
        rule = premiumRuleRepository.save(rule);
        return new PremiumRuleResponse(rule.getId(), rule.getRuleName(), rule.getAmount(), rule.getPremiumType());
    }

    @Transactional
    public Optional<String> assignDealerPremiumRules(Dealership dealership, List<DealerPremiumRuleInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }
        for (DealerPremiumRuleInput input : inputs) {
            PremiumRule rule = premiumRuleRepository.findById(input.ruleId()).orElse(null);
            if (rule == null) {
                return Optional.of("Premium rule not found: " + input.ruleId());
            }
            DealerPremium dp = new DealerPremium();
            dp.setDealership(dealership);
            dp.setRule(rule);
            dp.setStartDate(input.startDate());
            dp.setEndDate(input.endDate());
            dp.setLevel(input.levelOrDefault());
            dp.setNumberOfSales(input.numberOfSales());
            dealerPremiumRepository.save(dp);
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<DealerPremium> getEffectiveRuleForDealership(UUID dealershipId, LocalDate asOfDate) {
        List<DealerPremium> active = dealerPremiumRepository.findByDealership_IdAndStatusWithRuleOrderByLevelDesc(dealershipId, DealerPremiumStatus.ACTIVE);
        return active.stream()
                .filter(dp -> !asOfDate.isBefore(dp.getStartDate()))
                .filter(dp -> dp.getEndDate() == null || !asOfDate.isAfter(dp.getEndDate()))
                .findFirst();
    }

    @Transactional
    public void recordPremiumsForPurchase(Purchase purchase) {
        Dealership dealership = purchase.getDealership();
        if (dealership == null) return;
        UUID dealershipId = dealership.getId();
        LocalDate purchaseDate = purchase.getPurchaseDate();
        Optional<DealerPremium> effective = getEffectiveRuleForDealership(dealershipId, purchaseDate);
        if (effective.isEmpty()) {
            log.debug("No effective premium rule for dealership {} on purchase date {}", dealershipId, purchaseDate);
            return;
        }
        effective.ifPresent(dp -> {
            BigDecimal amount = computePremiumAmount(dp.getRule().getPremiumType(),
                    dp.getRule().getAmount(), purchase.getPurchasePrice());
            PurchasePremium pp = new PurchasePremium(purchase, dealership, dp.getRule(), amount);
            purchasePremiumRepository.save(pp);
        });
    }

    private static BigDecimal computePremiumAmount(PremiumType type, BigDecimal ruleAmount, BigDecimal purchasePrice) {
        if (type == PremiumType.FLAT) {
            return ruleAmount != null ? ruleAmount : BigDecimal.ZERO;
        }
        if (ruleAmount == null || purchasePrice == null) return BigDecimal.ZERO;
        return purchasePrice.multiply(ruleAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void expireDealerPremiumRulesIfApplicable(UUID dealershipId, LocalDate purchaseDate) {
        List<DealerPremium> active = dealerPremiumRepository.findByDealership_IdAndStatusOrderByLevelDesc(dealershipId, DealerPremiumStatus.ACTIVE);
        for (DealerPremium dp : active) {
            boolean expired = false;
            if (dp.getEndDate() != null && purchaseDate.isAfter(dp.getEndDate())) {
                expired = true;
            } else if (dp.getNumberOfSales() != null) {
                long count = purchaseRepository.countByDealership_IdAndPurchaseDateBetween(
                        dealershipId, dp.getStartDate(), purchaseDate);
                if (count >= dp.getNumberOfSales()) {
                    expired = true;
                }
            }
            if (expired) {
                dp.setStatus(DealerPremiumStatus.EXPIRED);
                dealerPremiumRepository.save(dp);
            }
        }
    }
}
