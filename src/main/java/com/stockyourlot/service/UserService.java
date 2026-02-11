package com.stockyourlot.service;

import com.stockyourlot.dto.DealershipRoleDto;
import com.stockyourlot.dto.UserWithRolesDto;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.DealershipUser;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.DealershipUserRepository;
import com.stockyourlot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final String DEFAULT_DEALERSHIP_ROLE = "ASSOCIATE";

    private final UserRepository userRepository;
    private final DealershipRepository dealershipRepository;
    private final DealershipUserRepository dealershipUserRepository;

    public UserService(UserRepository userRepository,
                       DealershipRepository dealershipRepository,
                       DealershipUserRepository dealershipUserRepository) {
        this.userRepository = userRepository;
        this.dealershipRepository = dealershipRepository;
        this.dealershipUserRepository = dealershipUserRepository;
    }

    /**
     * Add a user to a dealership with the given role (ASSOCIATE or ADMIN).
     * Defaults to ASSOCIATE if role is null or not provided.
     */
    @Transactional
    public User addUserToDealership(String email, UUID dealershipId, String role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));
        Dealership dealership = dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));

        String dealershipRole = (role != null && (role.equals("ADMIN") || role.equals("ASSOCIATE")))
                ? role : DEFAULT_DEALERSHIP_ROLE;

        var existing = dealershipUserRepository.findByUser_IdAndDealership_Id(user.getId(), dealershipId);
        if (existing.isPresent()) {
            DealershipUser du = existing.get();
            du.setDealershipRole(dealershipRole);
            dealershipUserRepository.save(du);
        } else {
            user.getDealershipUsers().add(new DealershipUser(user, dealership, dealershipRole));
            userRepository.save(user);
        }
        return userRepository.findById(user.getId()).orElseThrow();
    }

    /**
     * Returns all users with their global roles and dealership memberships.
     */
    @Transactional(readOnly = true)
    public List<UserWithRolesDto> getAllUsers() {
        return userRepository.findAllWithRolesAndDealershipUsers().stream()
                .map(user -> new UserWithRolesDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoleNames(),
                        user.getDealershipUsers().stream()
                                .map(du -> new DealershipRoleDto(
                                        du.getDealership().getId(),
                                        du.getDealership().getName(),
                                        du.getDealershipRole()))
                                .toList()))
                .toList();
    }
}
