package com.stockyourlot.repository;

import com.stockyourlot.entity.Dealership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DealershipRepository extends JpaRepository<Dealership, UUID> {

    Optional<Dealership> findByName(String name);
}
