package com.stockyourlot.service;

import com.stockyourlot.dto.DealershipRoleDto;
import com.stockyourlot.dto.UpdateUserRequest;
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
    private static final java.util.Set<String> GLOBAL_ROLES = java.util.Set.of("BUYER", "DEALER", "ADMIN");

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
     * Add a global role (BUYER, DEALER, ADMIN) to a user.
     * Idempotent: if the user already has the role, no error.
     */
    @Transactional
    public User addRoleToUser(String email, String roleName) {
        String normalized = roleName != null ? roleName.trim().toUpperCase() : "";
        if (!GLOBAL_ROLES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid role. Must be one of: BUYER, DEALER, ADMIN");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));
        Role role = roleRepository.findByName(normalized)
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
     * Update a user by ID. Only non-null fields in the request are applied.
     * Username and email must remain unique; 409 if already taken by another user.
     *
     * @throws ResponseStatusException 404 if user not found, 409 if username/email taken
     */
    @Transactional
    public UserWithRolesDto update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        if (request.username() != null && !request.username().trim().isEmpty()) {
            String username = request.username().trim();
            userRepository.findByUsername(username).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use: " + username);
                }
            });
            user.setUsername(username);
        }
        if (request.email() != null && !request.email().trim().isEmpty()) {
            String email = request.email().trim().toLowerCase();
            userRepository.findByEmail(email).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use: " + email);
                }
            });
            user.setEmail(email);
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim().isEmpty() ? null : request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim().isEmpty() ? null : request.lastName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim().isEmpty() ? null : request.phoneNumber().trim());
        }

        userRepository.save(user);
        return getById(id);
    }

    /**
     * Returns a user by ID with global roles and dealership memberships.
     * @throws ResponseStatusException 404 if not found
     */
    @Transactional(readOnly = true)
    public UserWithRolesDto getById(UUID id) {
        User user = userRepository.findByIdWithDealershipUsers(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        return new UserWithRolesDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRoleNames(),
                user.getDealershipUsers().stream()
                        .map(du -> new DealershipRoleDto(
                                du.getDealership().getId(),
                                du.getDealership().getName(),
                                du.getDealershipRole()))
                        .toList());
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
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
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
