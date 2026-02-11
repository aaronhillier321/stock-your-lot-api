package com.stockyourlot.repository;

import com.stockyourlot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.dealershipUsers du LEFT JOIN FETCH du.dealership WHERE u.id = :id")
    Optional<User> findByIdWithDealershipUsers(@Param("id") UUID id);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.dealershipUsers du LEFT JOIN FETCH du.dealership")
    List<User> findAllWithRolesAndDealershipUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
