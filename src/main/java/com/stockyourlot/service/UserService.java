package com.stockyourlot.service;

import com.stockyourlot.dto.DealershipRoleDto;
import com.stockyourlot.dto.UserWithRolesDto;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.DealershipUser;
import com.stockyourlot.entity.Role;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.DealershipUserRepository;
import com.stockyourlot.repository.RoleRepository;
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
    private static final java.util.Set<String> GLOBAL_ROLES = java.util.Set.of("USER", "SALES_ASSOCIATE", "SALES_ADMIN");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealershipRepository dealershipRepository;
    private final DealershipUserRepository dealershipUserRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DealershipRepository dealershipRepository,
                       DealershipUserRepository dealershipUserRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dealershipRepository = dealershipRepository;
        this.dealershipUserRepository = dealershipUserRepository;
    }

    /**
     * Add a global role (USER, SALES_ASSOCIATE, SALES_ADMIN) to a user.
     * Idempotent: if the user already has the role, no error.
     */
    @Transactional
    public User addRoleToUser(String email, String roleName) {
        if (!GLOBAL_ROLES.contains(roleName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid role. Must be one of: USER, SALES_ASSOCIATE, SALES_ADMIN");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + roleName));
        user.getRoles().add(role);
        return userRepository.save(user);
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
     * Remove a user completely. Deletes the user and all related data (roles, dealership
     * memberships, and any invites where they are the invited user). Invites where they
     * were the inviter will have inviter_id set to null by the DB.
     *
     * @throws ResponseStatusException 404 if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        userRepository.delete(user);
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
