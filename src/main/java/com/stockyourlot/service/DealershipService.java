package com.stockyourlot.service;

import com.stockyourlot.dto.AddDealerPremiumRequest;
import com.stockyourlot.dto.CreateDealershipRequest;
import com.stockyourlot.dto.DealerPremiumRuleInput;
import com.stockyourlot.dto.DealershipPremiumSummaryDto;
import com.stockyourlot.dto.DealershipResponse;
import com.stockyourlot.dto.DealerPremiumAssignmentDto;
import com.stockyourlot.dto.UpdateDealerPremiumRequest;
import com.stockyourlot.dto.UpdateDealershipRequest;
import com.stockyourlot.dto.UserAtDealershipDto;
import com.stockyourlot.entity.DealerPremium;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.DealershipUser;
import com.stockyourlot.repository.DealerPremiumRepository;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.DealershipUserRepository;
import com.stockyourlot.repository.PurchasePremiumRepository;
import com.stockyourlot.repository.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DealershipService {

    private final DealershipRepository dealershipRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePremiumRepository purchasePremiumRepository;
    private final DealerPremiumRepository dealerPremiumRepository;
    private final DealershipUserRepository dealershipUserRepository;
    private final PremiumService premiumService;

    public DealershipService(DealershipRepository dealershipRepository, PurchaseRepository purchaseRepository,
                             PurchasePremiumRepository purchasePremiumRepository,
                             DealerPremiumRepository dealerPremiumRepository,
                             DealershipUserRepository dealershipUserRepository,
                             PremiumService premiumService) {
        this.dealershipRepository = dealershipRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchasePremiumRepository = purchasePremiumRepository;
        this.dealerPremiumRepository = dealerPremiumRepository;
        this.dealershipUserRepository = dealershipUserRepository;
        this.premiumService = premiumService;
    }

    @Transactional
    public DealershipResponse create(CreateDealershipRequest request) {
        Dealership dealership = new Dealership(request.name());
        dealership.setAddressLine1(request.addressLine1());
        dealership.setAddressLine2(request.addressLine2());
        dealership.setCity(request.city());
        dealership.setState(request.state());
        dealership.setPostalCode(request.postalCode());
        dealership.setPhone(request.phone());
        dealership = dealershipRepository.save(dealership);
        Optional<String> premiumError = premiumService.assignDealerPremiumRules(dealership, request.dealerPremiumRules());
        if (premiumError.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, premiumError.get());
        }
        return getById(dealership.getId());
    }

    @Transactional(readOnly = true)
    public DealershipResponse getById(UUID id) {
        Dealership dealership = dealershipRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + id));
        long purchaseCount = purchaseRepository.countByDealership_Id(id);
        List<DealerPremiumAssignmentDto> premiumDtos = dealerPremiumRepository.findByDealership_IdWithRuleOrderByLevelDesc(id)
                .stream()
                .map(this::toDealerPremiumAssignmentDto)
                .toList();
        return toResponse(dealership, purchaseCount, premiumDtos);
    }

    @Transactional(readOnly = true)
    public DealershipPremiumSummaryDto getPremiumSummary(UUID dealershipId) {
        dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));
        YearMonth currentMonth = YearMonth.now();
        long purchasesThisMonth = purchaseRepository.countByDealership_IdAndPurchaseDateBetween(
                dealershipId, currentMonth.atDay(1), currentMonth.atEndOfMonth());
        BigDecimal totalPremiums = purchasePremiumRepository.sumAmountByDealership_Id(dealershipId);
        if (totalPremiums == null) totalPremiums = BigDecimal.ZERO;
        return new DealershipPremiumSummaryDto(purchasesThisMonth, totalPremiums, totalPremiums);
    }

    @Transactional(readOnly = true)
    public List<DealerPremiumAssignmentDto> getDealerPremiumAssignmentsByDealershipId(UUID dealershipId) {
        if (!dealershipRepository.existsById(dealershipId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId);
        }
        return dealerPremiumRepository.findByDealership_IdWithRuleOrderByLevelDesc(dealershipId)
                .stream()
                .map(this::toDealerPremiumAssignmentDto)
                .toList();
    }

    @Transactional
    public DealerPremiumAssignmentDto addDealerPremium(UUID dealershipId, AddDealerPremiumRequest request) {
        Dealership dealership = dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));
        DealerPremiumRuleInput input = new DealerPremiumRuleInput(
                request.ruleId(),
                request.startDate(),
                request.endDate(),
                request.level(),
                request.numberOfSales());
        DealerPremium dp = premiumService.addDealerPremium(dealership, input);
        return toDealerPremiumAssignmentDto(dp);
    }

    @Transactional
    public DealerPremiumAssignmentDto updateDealerPremium(UUID dealershipId, UUID premiumId, UpdateDealerPremiumRequest request) {
        DealerPremium dp = premiumService.updateDealerPremium(dealershipId, premiumId, request);
        return toDealerPremiumAssignmentDto(dp);
    }

    @Transactional
    public void removeDealerPremium(UUID dealershipId, UUID premiumId) {
        premiumService.deleteDealerPremium(dealershipId, premiumId);
    }

    @Transactional(readOnly = true)
    public List<UserAtDealershipDto> getUsersByDealershipId(UUID dealershipId) {
        dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));
        return dealershipUserRepository.findByDealership_IdWithUser(dealershipId)
                .stream()
                .map(du -> new UserAtDealershipDto(
                        du.getUser().getId(),
                        du.getUser().getUsername(),
                        du.getUser().getEmail(),
                        du.getDealershipRole()))
                .toList();
    }

    private DealerPremiumAssignmentDto toDealerPremiumAssignmentDto(DealerPremium dp) {
        var rule = dp.getRule();
        return new DealerPremiumAssignmentDto(
                dp.getId(),
                rule != null ? rule.getId() : null,
                rule != null ? rule.getAmount() : null,
                rule != null ? rule.getPremiumType() : null,
                dp.getStartDate(),
                dp.getEndDate(),
                dp.getLevel(),
                dp.getNumberOfSales(),
                dp.getStatus());
    }

    @Transactional
    public DealershipResponse update(UUID id, UpdateDealershipRequest request) {
        Dealership dealership = dealershipRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + id));
        if (request.name() != null) dealership.setName(request.name());
        if (request.addressLine1() != null) dealership.setAddressLine1(request.addressLine1());
        if (request.addressLine2() != null) dealership.setAddressLine2(request.addressLine2());
        if (request.city() != null) dealership.setCity(request.city());
        if (request.state() != null) dealership.setState(request.state());
        if (request.postalCode() != null) dealership.setPostalCode(request.postalCode());
        if (request.phone() != null) dealership.setPhone(request.phone());
        dealership = dealershipRepository.save(dealership);
        if (request.dealerPremiumRules() != null) {
            dealerPremiumRepository.deleteByDealership_Id(id);
            Optional<String> premiumError = premiumService.assignDealerPremiumRules(dealership, request.dealerPremiumRules());
            if (premiumError.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, premiumError.get());
            }
        }
        return getById(id);
    }

    @Transactional(readOnly = true)
    public List<DealershipResponse> getAll() {
        List<Dealership> list = dealershipRepository.findAll();
        if (list.isEmpty()) return List.of();
        List<UUID> ids = list.stream().map(Dealership::getId).toList();
        Map<UUID, Long> counts = purchaseRepository.countByDealershipIds(ids).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
        List<DealerPremium> activePremiums = dealerPremiumRepository.findByDealership_IdInAndStatusWithRuleOrderByLevelDesc(
                ids, com.stockyourlot.entity.DealerPremiumStatus.ACTIVE);
        Map<UUID, List<DealerPremiumAssignmentDto>> premiumsByDealership = activePremiums.stream()
                .collect(Collectors.groupingBy(dp -> dp.getDealership().getId(),
                        Collectors.mapping(this::toDealerPremiumAssignmentDto, Collectors.toList())));
        return list.stream()
                .map(d -> toResponse(d, counts.getOrDefault(d.getId(), 0L), premiumsByDealership.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    private DealershipResponse toResponse(Dealership d, long purchaseCount, List<DealerPremiumAssignmentDto> dealerPremiumRules) {
        return new DealershipResponse(
                d.getId(),
                d.getName(),
                d.getAddressLine1(),
                d.getAddressLine2(),
                d.getCity(),
                d.getState(),
                d.getPostalCode(),
                d.getPhone(),
                d.getCreatedAt(),
                purchaseCount,
                dealerPremiumRules != null ? dealerPremiumRules : List.of()
        );
    }
}
