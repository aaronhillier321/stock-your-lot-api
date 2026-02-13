package com.stockyourlot.service;

import com.stockyourlot.dto.CreatePurchaseRequest;
import com.stockyourlot.dto.PurchaseResponse;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.Purchase;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.PurchaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final DealershipRepository dealershipRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, DealershipRepository dealershipRepository) {
        this.purchaseRepository = purchaseRepository;
        this.dealershipRepository = dealershipRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAll() {
        return purchaseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getByBuyerId(UUID buyerId) {
        return purchaseRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getByDealershipId(UUID dealershipId) {
        return purchaseRepository.findByDealership_IdOrderByCreatedAtDesc(dealershipId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponse create(CreatePurchaseRequest request, User buyer) {
        Dealership dealership = dealershipRepository.findById(request.dealershipId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dealership not found: " + request.dealershipId()));
        Purchase p = new Purchase();
        p.setBuyer(buyer);
        p.setDealership(dealership);
        p.setPurchaseDate(request.date());
        p.setAuctionPlatform(request.auctionPlatform());
        p.setVin(request.vin());
        p.setMiles(request.miles());
        p.setPurchasePrice(request.purchasePrice());
        p.setVehicleYear(request.vehicleYear());
        p.setVehicleMake(request.vehicleMake());
        p.setVehicleModel(request.vehicleModel());
        p.setVehicleTrimLevel(request.vehicleTrimLevel());
        p.setTransportQuote(request.transportQuote());
        p = purchaseRepository.save(p);
        return toResponse(p);
    }

    private PurchaseResponse toResponse(Purchase p) {
        Dealership d = p.getDealership();
        User buyer = p.getBuyer();
        return new PurchaseResponse(
                p.getId(),
                buyer != null ? buyer.getId() : null,
                buyer != null ? buyer.getUsername() : null,
                buyer != null ? buyer.getEmail() : null,
                d != null ? d.getId() : null,
                d != null ? d.getName() : null,
                p.getPurchaseDate(),
                p.getAuctionPlatform(),
                p.getVin(),
                p.getMiles(),
                p.getPurchasePrice(),
                p.getVehicleYear(),
                p.getVehicleMake(),
                p.getVehicleModel(),
                p.getVehicleTrimLevel(),
                p.getTransportQuote(),
                p.getCreatedAt()
        );
    }
}
