package com.stockyourlot.service;

import com.stockyourlot.dto.CommissionRuleResponse;
import com.stockyourlot.dto.CreateCommissionRuleRequest;
import com.stockyourlot.dto.UpdateCommissionRuleRequest;
import com.stockyourlot.dto.UpdateUserCommissionRequest;
import com.stockyourlot.dto.UserCommissionRuleInput;
import com.stockyourlot.entity.CommissionRule;
import com.stockyourlot.entity.CommissionType;
import com.stockyourlot.entity.Purchase;
import com.stockyourlot.entity.User;
import com.stockyourlot.entity.PurchaseCommission;
import com.stockyourlot.entity.UserCommission;
import com.stockyourlot.entity.UserCommissionStatus;
import com.stockyourlot.repository.CommissionRuleRepository;
import com.stockyourlot.repository.PurchaseCommissionRepository;
import com.stockyourlot.repository.PurchaseRepository;
import com.stockyourlot.repository.UserCommissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Commission rules and user assignments. Expires rules (1) on the Xth sale when
 * number_of_sales = X, or (2) on the first sale after end_date.
 */
@Service
public class CommissionService {

    private static final Logger log = LoggerFactory.getLogger(CommissionService.class);

    private final CommissionRuleRepository commissionRuleRepository;
    private final UserCommissionRepository userCommissionRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseCommissionRepository purchaseCommissionRepository;

    public CommissionService(CommissionRuleRepository commissionRuleRepository,
                             UserCommissionRepository userCommissionRepository,
                             PurchaseRepository purchaseRepository,
                             PurchaseCommissionRepository purchaseCommissionRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.userCommissionRepository = userCommissionRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchaseCommissionRepository = purchaseCommissionRepository;
    }

    @Transactional(readOnly = true)
    public List<CommissionRuleResponse> getAllRules() {
        return commissionRuleRepository.findAll().stream()
                .map(r -> new CommissionRuleResponse(r.getId(), r.getRuleName(), r.getAmount(), r.getCommissionType()))
                .toList();
    }

    @Transactional
    public CommissionRuleResponse createRule(CreateCommissionRuleRequest request) {
        CommissionRule rule = new CommissionRule();
        rule.setRuleName(request.ruleName() != null ? request.ruleName().trim() : "");
        rule.setAmount(request.amount());
        rule.setCommissionType(request.commissionType());
        rule = commissionRuleRepository.save(rule);
        return new CommissionRuleResponse(rule.getId(), rule.getRuleName(), rule.getAmount(), rule.getCommissionType());
    }

    @Transactional
    public CommissionRuleResponse updateRule(UUID id, UpdateCommissionRuleRequest request) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission rule not found: " + id));
        if (request.ruleName() != null) rule.setRuleName(request.ruleName().trim());
        if (request.amount() != null) rule.setAmount(request.amount());
        if (request.commissionType() != null) rule.setCommissionType(request.commissionType());
        rule = commissionRuleRepository.save(rule);
        return new CommissionRuleResponse(rule.getId(), rule.getRuleName(), rule.getAmount(), rule.getCommissionType());
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!commissionRuleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission rule not found: " + id);
        }
        commissionRuleRepository.deleteById(id);
    }

    /**
     * Creates user commission assignments for the given user from the input list.
     * @return Optional error message if a rule is not found (e.g. for conflict or validation response)
     */
    @Transactional
    public Optional<String> assignUserCommissionRules(User user, List<UserCommissionRuleInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }
        for (UserCommissionRuleInput input : inputs) {
            CommissionRule rule = commissionRuleRepository.findById(input.ruleId()).orElse(null);
            if (rule == null) {
                return Optional.of("Commission rule not found: " + input.ruleId());
            }
            int level = input.levelOrDefault();
            if (userCommissionRepository.existsByUser_IdAndStatusAndLevel(user.getId(), UserCommissionStatus.ACTIVE, level)) {
                return Optional.of("User already has an active commission rule at level " + level + ". Only one active rule per level is allowed.");
            }
            UserCommission uc = new UserCommission();
            uc.setUser(user);
            uc.setRule(rule);
            uc.setStartDate(input.startDate());
            uc.setEndDate(input.endDate());
            uc.setLevel(level);
            uc.setNumberOfSales(input.numberOfSales());
            userCommissionRepository.save(uc);
        }
        return Optional.empty();
    }

    /**
     * Add a single commission assignment for a user. Validates rule exists and no active assignment at same level.
     * @return The created UserCommission
     */
    @Transactional
    public UserCommission addUserCommission(User user, UserCommissionRuleInput input) {
        CommissionRule rule = commissionRuleRepository.findById(input.ruleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission rule not found: " + input.ruleId()));
        int level = input.levelOrDefault();
        if (userCommissionRepository.existsByUser_IdAndStatusAndLevel(user.getId(), UserCommissionStatus.ACTIVE, level)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User already has an active commission rule at level " + level + ". Only one active rule per level is allowed.");
        }
        UserCommission uc = new UserCommission();
        uc.setUser(user);
        uc.setRule(rule);
        uc.setStartDate(input.startDate());
        uc.setEndDate(input.endDate());
        uc.setLevel(level);
        uc.setNumberOfSales(input.numberOfSales());
        return userCommissionRepository.save(uc);
    }

    /**
     * Update an existing user commission assignment. Assignment must belong to the given user.
     */
    @Transactional
    public UserCommission updateUserCommission(UUID userId, UUID commissionId, UpdateUserCommissionRequest request) {
        UserCommission uc = userCommissionRepository.findByUser_IdAndId(userId, commissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User commission not found: " + commissionId));
        if (request.startDate() != null) uc.setStartDate(request.startDate());
        if (request.endDate() != null) uc.setEndDate(request.endDate());
        if (request.level() != null && request.level() >= 0) uc.setLevel(request.level());
        if (request.numberOfSales() != null) uc.setNumberOfSales(request.numberOfSales());
        return userCommissionRepository.save(uc);
    }

    /**
     * Delete a user commission assignment. Assignment must belong to the given user.
     */
    @Transactional
    public void deleteUserCommission(UUID userId, UUID commissionId) {
        UserCommission uc = userCommissionRepository.findByUser_IdAndId(userId, commissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User commission not found: " + commissionId));
        userCommissionRepository.delete(uc);
    }

    /**
     * Returns the single effective commission rule for a user on a given date:
     * active, within start/end range, highest level. Empty if none.
     */
    @Transactional(readOnly = true)
    public Optional<UserCommission> getEffectiveRuleForUser(UUID userId, LocalDate asOfDate) {
        List<UserCommission> active = userCommissionRepository.findByUser_IdAndStatusWithRuleOrderByLevelDesc(userId, UserCommissionStatus.ACTIVE);
        return active.stream()
                .filter(uc -> !asOfDate.isBefore(uc.getStartDate()))
                .filter(uc -> uc.getEndDate() == null || !asOfDate.isAfter(uc.getEndDate()))
                .findFirst();
    }

    /**
     * Call after a purchase is created. Records commission for the buyer (and any other
     * recipients) based on their effective rule. Amount is stored flat (percent of
     * purchase price computed here). Call this before expireUserCommissionRulesIfApplicable
     * so the rule is still active when we look it up.
     */
    @Transactional
    public void recordCommissionsForPurchase(Purchase purchase) {
        UUID buyerId = purchase.getBuyer().getId();
        LocalDate purchaseDate = purchase.getPurchaseDate();
        Optional<UserCommission> effective = getEffectiveRuleForUser(buyerId, purchaseDate);
        if (effective.isEmpty()) {
            log.debug("No effective commission rule for buyer {} on purchase date {}; ensure user has an ACTIVE assignment with startDate <= {} and (endDate null or >= {})",
                    buyerId, purchaseDate, purchaseDate, purchaseDate);
        }
        effective.ifPresent(uc -> {
            BigDecimal amount = computeCommissionAmount(uc.getRule().getCommissionType(),
                    uc.getRule().getAmount(), purchase.getPurchasePrice());
            PurchaseCommission pc = new PurchaseCommission(
                    purchase, purchase.getBuyer(), uc.getRule(), amount);
            purchaseCommissionRepository.save(pc);
        });
    }

    private static BigDecimal computeCommissionAmount(CommissionType type, BigDecimal ruleAmount, BigDecimal purchasePrice) {
        if (type == CommissionType.FLAT) {
            return ruleAmount != null ? ruleAmount : BigDecimal.ZERO;
        }
        // PERCENT: rule amount is the percentage (e.g. 5 for 5%)
        if (ruleAmount == null || purchasePrice == null) return BigDecimal.ZERO;
        return purchasePrice.multiply(ruleAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Call after a purchase is created. Expires user commission assignments for the buyer when applicable:
     * (1) if purchase date is after end_date, set status to EXPIRED;
     * (2) if number_of_sales is set and count of buyer's sales from start_date through purchase date >= number_of_sales, set status to EXPIRED.
     */
    @Transactional
    public void expireUserCommissionRulesIfApplicable(UUID buyerId, LocalDate purchaseDate) {
        List<UserCommission> active = userCommissionRepository.findByUser_IdAndStatusOrderByLevelDesc(buyerId, UserCommissionStatus.ACTIVE);
        for (UserCommission uc : active) {
            boolean expired = false;
            if (uc.getEndDate() != null && purchaseDate.isAfter(uc.getEndDate())) {
                expired = true;
            } else if (uc.getNumberOfSales() != null) {
                long count = purchaseRepository.countByBuyer_IdAndPurchaseDateBetween(
                        buyerId, uc.getStartDate(), purchaseDate);
                if (count >= uc.getNumberOfSales()) {
                    expired = true;
                }
            }
            if (expired) {
                uc.setStatus(UserCommissionStatus.EXPIRED);
                userCommissionRepository.save(uc);
            }
        }
    }
}
