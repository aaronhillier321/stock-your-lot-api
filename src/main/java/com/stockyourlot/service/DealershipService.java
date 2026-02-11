package com.stockyourlot.service;

import com.stockyourlot.dto.CreateDealershipRequest;
import com.stockyourlot.dto.DealershipResponse;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.repository.DealershipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DealershipService {

    private final DealershipRepository dealershipRepository;

    public DealershipService(DealershipRepository dealershipRepository) {
        this.dealershipRepository = dealershipRepository;
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
        return toResponse(dealership);
    }

    @Transactional(readOnly = true)
    public DealershipResponse getById(UUID id) {
        Dealership dealership = dealershipRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + id));
        return toResponse(dealership);
    }

    @Transactional(readOnly = true)
    public List<DealershipResponse> getAll() {
        return dealershipRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private DealershipResponse toResponse(Dealership d) {
        return new DealershipResponse(
                d.getId(),
                d.getName(),
                d.getAddressLine1(),
                d.getAddressLine2(),
                d.getCity(),
                d.getState(),
                d.getPostalCode(),
                d.getPhone(),
                d.getCreatedAt()
        );
    }
}
