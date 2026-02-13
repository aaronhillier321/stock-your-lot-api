package com.stockyourlot.service;

import com.stockyourlot.dto.CreatePurchaseRequest;
import com.stockyourlot.dto.PurchaseResponse;
import com.stockyourlot.entity.Purchase;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.PurchaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
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

    @Transactional
    public PurchaseResponse create(CreatePurchaseRequest request, User buyer) {
        Purchase p = new Purchase();
        p.setBuyer(buyer);
        p.setDealership(request.dealership());
        p.setPurchaseDate(request.date());
        p.setAuctionPlatform(request.auctionPlatform());
        p.setVin(request.vin());
        p.setYmmt(request.ymmt());
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
        return new PurchaseResponse(
                p.getId(),
                p.getBuyer().getId(),
                p.getDealership(),
                p.getPurchaseDate(),
                p.getAuctionPlatform(),
                p.getVin(),
                p.getYmmt(),
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
