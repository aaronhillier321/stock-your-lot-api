package com.stockyourlot.service;

import com.stockyourlot.dto.CreateDealershipRequest;
import com.stockyourlot.dto.DealershipResponse;
import com.stockyourlot.dto.UpdateDealershipRequest;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DealershipService {

    private final DealershipRepository dealershipRepository;
    private final PurchaseRepository purchaseRepository;

    public DealershipService(DealershipRepository dealershipRepository, PurchaseRepository purchaseRepository) {
        this.dealershipRepository = dealershipRepository;
        this.purchaseRepository = purchaseRepository;
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
        return toResponse(dealership, 0L);
    }

    @Transactional(readOnly = true)
    public DealershipResponse getById(UUID id) {
        Dealership dealership = dealershipRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + id));
        long purchaseCount = purchaseRepository.countByDealership_Id(id);
        return toResponse(dealership, purchaseCount);
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
        long purchaseCount = purchaseRepository.countByDealership_Id(id);
        return toResponse(dealership, purchaseCount);
    }

    @Transactional(readOnly = true)
    public List<DealershipResponse> getAll() {
        List<Dealership> list = dealershipRepository.findAll();
        List<UUID> ids = list.stream().map(Dealership::getId).toList();
        Map<UUID, Long> counts = ids.isEmpty() ? Map.of() : purchaseRepository.countByDealershipIds(ids).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
        return list.stream()
                .map(d -> toResponse(d, counts.getOrDefault(d.getId(), 0L)))
                .toList();
    }

    private DealershipResponse toResponse(Dealership d, long purchaseCount) {
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
                purchaseCount
        );
    }
}
