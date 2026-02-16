package com.stockyourlot.service;

import com.stockyourlot.dto.AddUserCommissionRequest;
import com.stockyourlot.dto.DealershipRoleDto;
import com.stockyourlot.dto.UpdateUserCommissionRequest;
import com.stockyourlot.dto.UpdateUserRequest;
import com.stockyourlot.dto.UserCommissionAssignmentDto;
import com.stockyourlot.dto.UserCommissionRuleInput;
import com.stockyourlot.dto.UserWithRolesDto;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.DealershipUser;
import com.stockyourlot.entity.Role;
import com.stockyourlot.entity.User;
import com.stockyourlot.entity.UserCommission;
import com.stockyourlot.entity.UserCommissionStatus;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.DealershipUserRepository;
import com.stockyourlot.repository.RoleRepository;
import com.stockyourlot.repository.UserCommissionRepository;
import com.stockyourlot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String DEFAULT_DEALERSHIP_ROLE = "BUYER";
    private static final java.util.Set<String> GLOBAL_ROLES = java.util.Set.of("BUYER", "DEALER", "ADMIN");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CommissionService commissionService;
    private final UserCommissionRepository userCommissionRepository;
    private final DealershipRepository dealershipRepository;
    private final DealershipUserRepository dealershipUserRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CommissionService commissionService,
                       UserCommissionRepository userCommissionRepository,
                       DealershipRepository dealershipRepository,
                       DealershipUserRepository dealershipUserRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.commissionService = commissionService;
        this.userCommissionRepository = userCommissionRepository;
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
     * Add a user to a dealership with the given role (BUYER or ADMIN).
     * Defaults to BUYER if role is null or not provided.
     */
    @Transactional
    public User addUserToDealership(String email, UUID dealershipId, String role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));
        addUserDealership(user.getId(), dealershipId, role);
        return userRepository.findById(user.getId()).orElseThrow();
    }

    /**
     * Add a user (by id) to a dealership with the given role. If already a member, updates the role.
     * @return The created or updated membership as DealershipRoleDto
     */
    @Transactional
    public DealershipRoleDto addUserDealership(UUID userId, UUID dealershipId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Dealership dealership = dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));

        String dealershipRole = (role != null && (role.equals("ADMIN") || role.equals("BUYER")))
                ? role : DEFAULT_DEALERSHIP_ROLE;

        var existing = dealershipUserRepository.findByUser_IdAndDealership_Id(userId, dealershipId);
        DealershipUser du;
        if (existing.isPresent()) {
            du = existing.get();
            du.setDealershipRole(dealershipRole);
            dealershipUserRepository.save(du);
        } else {
            du = new DealershipUser(user, dealership, dealershipRole);
            user.getDealershipUsers().add(du);
            userRepository.save(user);
        }
        return new DealershipRoleDto(dealership.getId(), dealership.getName(), du.getDealershipRole());
    }

    /**
     * Update a user's role at a dealership. Membership must already exist.
     * @return The updated membership as DealershipRoleDto
     */
    @Transactional
    public DealershipRoleDto updateUserDealership(UUID userId, UUID dealershipId, String role) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        if (!dealershipRepository.existsById(dealershipId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId);
        }
        DealershipUser du = dealershipUserRepository.findByUser_IdAndDealership_Id(userId, dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a member of this dealership"));
        if (role != null && (role.equals("ADMIN") || role.equals("BUYER"))) {
            du.setDealershipRole(role);
            dealershipUserRepository.save(du);
        }
        return new DealershipRoleDto(du.getDealership().getId(), du.getDealership().getName(), du.getDealershipRole());
    }

    /**
     * Remove a user from a dealership (removes the dealership membership).
     *
     * @throws ResponseStatusException 404 if user or dealership not found
     */
    @Transactional
    public void removeUserFromDealership(UUID userId, UUID dealershipId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        if (!dealershipRepository.existsById(dealershipId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId);
        }
        dealershipUserRepository.deleteByUser_IdAndDealership_Id(userId, dealershipId);
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
     * @throws ResponseStatusException 404 if user not found, 409 if email taken
     */
    @Transactional
    public UserWithRolesDto update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

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

        if (request.roles() != null) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.roles()) {
                String normalized = roleName != null ? roleName.trim().toUpperCase() : "";
                if (normalized.isEmpty()) continue;
                if (!GLOBAL_ROLES.contains(normalized)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid role: " + roleName + ". Must be one of: BUYER, DEALER, ADMIN");
                }
                Role role = roleRepository.findByName(normalized)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + normalized));
                newRoles.add(role);
            }
            user.setRoles(newRoles);
        }

        if (request.userCommissionRules() != null) {
            userCommissionRepository.deleteByUser_Id(id);
            Optional<String> commissionError = commissionService.assignUserCommissionRules(user, request.userCommissionRules());
            if (commissionError.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, commissionError.get());
            }
        }

        userRepository.save(user);
        return getById(id);
    }

    /**
     * Returns dealership memberships (roles) for a user by ID.
     * @throws ResponseStatusException 404 if user not found
     */
    @Transactional(readOnly = true)
    public List<DealershipRoleDto> getDealershipRolesByUserId(UUID id) {
        User user = userRepository.findByIdWithDealershipUsers(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        return user.getDealershipUsers().stream()
                .map(du -> new DealershipRoleDto(
                        du.getDealership().getId(),
                        du.getDealership().getName(),
                        du.getDealershipRole()))
                .toList();
    }

    /**
     * Get all commission assignments for a user by ID.
     */
    @Transactional(readOnly = true)
    public List<UserCommissionAssignmentDto> getCommissionAssignmentsByUserId(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        return userCommissionRepository.findByUser_IdWithRuleOrderByLevelDesc(id)
                .stream()
                .map(this::toUserCommissionAssignmentDto)
                .toList();
    }

    /**
     * Add a commission assignment for a user (by user id).
     */
    @Transactional
    public UserCommissionAssignmentDto addUserCommission(UUID userId, AddUserCommissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        UserCommissionRuleInput input = new UserCommissionRuleInput(
                request.ruleId(),
                request.startDate(),
                request.endDate(),
                request.level(),
                request.numberOfSales());
        UserCommission uc = commissionService.addUserCommission(user, input);
        return toUserCommissionAssignmentDto(uc);
    }

    /**
     * Update a user commission assignment. Assignment must belong to the given user.
     */
    @Transactional
    public UserCommissionAssignmentDto updateUserCommission(UUID userId, UUID commissionId, UpdateUserCommissionRequest request) {
        UserCommission uc = commissionService.updateUserCommission(userId, commissionId, request);
        return toUserCommissionAssignmentDto(uc);
    }

    /**
     * Remove a user commission assignment. Assignment must belong to the given user.
     */
    @Transactional
    public void removeUserCommission(UUID userId, UUID commissionId) {
        commissionService.deleteUserCommission(userId, commissionId);
    }

    /**
     * Returns a user by ID with global roles, dealership memberships, and all commission rule assignments.
     * @throws ResponseStatusException 404 if not found
     */
    @Transactional(readOnly = true)
    public UserWithRolesDto getById(UUID id) {
        User user = userRepository.findByIdWithDealershipUsers(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        List<UserCommissionAssignmentDto> commissionDtos = userCommissionRepository.findByUser_IdWithRuleOrderByLevelDesc(id)
                .stream()
                .map(this::toUserCommissionAssignmentDto)
                .toList();
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
                        .toList(),
                commissionDtos);
    }

    private UserCommissionAssignmentDto toUserCommissionAssignmentDto(UserCommission uc) {
        var rule = uc.getRule();
        return new UserCommissionAssignmentDto(
                uc.getId(),
                rule != null ? rule.getId() : null,
                rule != null ? rule.getAmount() : null,
                rule != null ? rule.getCommissionType() : null,
                uc.getStartDate(),
                uc.getEndDate(),
                uc.getLevel(),
                uc.getNumberOfSales(),
                uc.getStatus());
    }

    /**
     * Returns all users with their global roles, dealership memberships, and active commission rule assignments.
     */
    @Transactional(readOnly = true)
    public List<UserWithRolesDto> getAllUsers() {
        List<User> users = userRepository.findAllWithRolesAndDealershipUsers();
        if (users.isEmpty()) return List.of();
        Set<UUID> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        List<UserCommission> activeCommissions = userCommissionRepository
                .findByUser_IdInAndStatusWithRuleOrderByLevelDesc(userIds, UserCommissionStatus.ACTIVE);
        Map<UUID, List<UserCommissionAssignmentDto>> commissionsByUser = activeCommissions.stream()
                .collect(Collectors.groupingBy(uc -> uc.getUser().getId(),
                        Collectors.mapping(this::toUserCommissionAssignmentDto, Collectors.toList())));

        return users.stream()
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
                                .toList(),
                        commissionsByUser.getOrDefault(user.getId(), List.of())))
                .toList();
    }
}
