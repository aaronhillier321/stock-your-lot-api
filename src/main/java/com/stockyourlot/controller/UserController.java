package com.stockyourlot.controller;

import com.stockyourlot.dto.AddDealershipUserRequest;
import com.stockyourlot.dto.AddRoleRequest;
import com.stockyourlot.dto.AddUserCommissionRequest;
import com.stockyourlot.dto.AddUserDealershipRequest;
import com.stockyourlot.dto.DealershipRoleDto;
import com.stockyourlot.dto.UpdateUserCommissionRequest;
import com.stockyourlot.dto.UpdateUserDealershipRequest;
import com.stockyourlot.dto.UpdateUserRequest;
import com.stockyourlot.dto.UserCommissionAssignmentDto;
import com.stockyourlot.dto.UserRolesResponse;
import com.stockyourlot.dto.UserWithRolesDto;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users with their global roles and dealership memberships.
     */
    @GetMapping
    public ResponseEntity<List<UserWithRolesDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get a user by ID with global roles and dealership memberships.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserWithRolesDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * Get dealership roles (memberships) for a user by ID.
     */
    @GetMapping("/{id}/dealerships")
    public ResponseEntity<List<DealershipRoleDto>> getDealershipRolesByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getDealershipRolesByUserId(id));
    }

    /**
     * Add a user to a dealership (by user id). If already a member, updates the role.
     */
    @PostMapping("/{id}/dealerships")
    public ResponseEntity<DealershipRoleDto> addUserDealership(
            @PathVariable UUID id,
            @Valid @RequestBody AddUserDealershipRequest request) {
        return ResponseEntity.ok(userService.addUserDealership(id, request.dealershipId(), request.role()));
    }

    /**
     * Update a user's role at a dealership. User must already be a member.
     */
    @PutMapping("/{id}/dealerships/{dealershipId}")
    public ResponseEntity<DealershipRoleDto> updateUserDealership(
            @PathVariable UUID id,
            @PathVariable UUID dealershipId,
            @Valid @RequestBody UpdateUserDealershipRequest request) {
        return ResponseEntity.ok(userService.updateUserDealership(id, dealershipId, request.role()));
    }

    /**
     * Get commission assignments for a user by ID.
     */
    @GetMapping("/{id}/commissions")
    public ResponseEntity<List<UserCommissionAssignmentDto>> getCommissionsByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getCommissionAssignmentsByUserId(id));
    }

    /**
     * Add a commission assignment for a user (by user id).
     */
    @PostMapping("/{id}/commissions")
    public ResponseEntity<UserCommissionAssignmentDto> addUserCommission(
            @PathVariable UUID id,
            @Valid @RequestBody AddUserCommissionRequest request) {
        return ResponseEntity.ok(userService.addUserCommission(id, request));
    }

    /**
     * Update a user commission assignment. Assignment must belong to the given user.
     */
    @PutMapping("/{id}/commissions/{commissionId}")
    public ResponseEntity<UserCommissionAssignmentDto> updateUserCommission(
            @PathVariable UUID id,
            @PathVariable UUID commissionId,
            @Valid @RequestBody UpdateUserCommissionRequest request) {
        return ResponseEntity.ok(userService.updateUserCommission(id, commissionId, request));
    }

    /**
     * Remove a user commission assignment. Assignment must belong to the given user.
     */
    @DeleteMapping("/{id}/commissions/{commissionId}")
    public ResponseEntity<Void> removeUserCommission(
            @PathVariable UUID id,
            @PathVariable UUID commissionId) {
        userService.removeUserCommission(id, commissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a user by ID. Only provided fields are updated (partial update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserWithRolesDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /**
     * Add a global role (BUYER, DEALER, ADMIN) to a user.
     */
    @PostMapping("/roles")
    public ResponseEntity<UserRolesResponse> addRoleToUser(
            @Valid @RequestBody AddRoleRequest request) {
        User user = userService.addRoleToUser(request.email(), request.role());
        return ResponseEntity.ok(new UserRolesResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleNames()
        ));
    }

    /**
     * Add a user to a dealership with an optional role (BUYER or ADMIN).
     * Defaults to BUYER if role is not provided.
     */
    @PostMapping("/dealerships")
    public ResponseEntity<UserRolesResponse> addUserToDealership(
            @Valid @RequestBody AddDealershipUserRequest request) {
        User user = userService.addUserToDealership(
                request.email(),
                request.dealershipId(),
                request.role());
        return ResponseEntity.ok(new UserRolesResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleNames()
        ));
    }

    /**
     * Remove a user from a dealership (removes the dealership membership).
     */
    @DeleteMapping("/{id}/dealerships/{dealershipId}")
    public ResponseEntity<Void> removeUserFromDealership(
            @PathVariable UUID id,
            @PathVariable UUID dealershipId) {
        userService.removeUserFromDealership(id, dealershipId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove a user completely. Deletes the user and all related data (roles, dealership
     * memberships, invites where they are the invited user). Requires authentication.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
