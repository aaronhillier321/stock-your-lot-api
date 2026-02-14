package com.stockyourlot.service;

import com.stockyourlot.dto.CreatePurchaseRequest;
import com.stockyourlot.dto.PurchaseResponse;
import com.stockyourlot.dto.UpdatePurchaseRequest;
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
    public PurchaseResponse getById(UUID id) {
        Purchase p = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found: " + id));
        return toResponse(p);
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

    @Transactional
    public PurchaseResponse update(UUID purchaseId, UpdatePurchaseRequest request) {
        Purchase p = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found: " + purchaseId));
        if (request.dealershipId() != null) {
            Dealership dealership = dealershipRepository.findById(request.dealershipId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dealership not found: " + request.dealershipId()));
            p.setDealership(dealership);
        }
        if (request.date() != null) p.setPurchaseDate(request.date());
        if (request.auctionPlatform() != null) p.setAuctionPlatform(request.auctionPlatform());
        if (request.vin() != null) p.setVin(request.vin());
        if (request.miles() != null) p.setMiles(request.miles());
        if (request.purchasePrice() != null) p.setPurchasePrice(request.purchasePrice());
        if (request.vehicleYear() != null) p.setVehicleYear(request.vehicleYear());
        if (request.vehicleMake() != null) p.setVehicleMake(request.vehicleMake());
        if (request.vehicleModel() != null) p.setVehicleModel(request.vehicleModel());
        if (request.vehicleTrimLevel() != null) p.setVehicleTrimLevel(request.vehicleTrimLevel());
        if (request.transportQuote() != null) p.setTransportQuote(request.transportQuote());
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
