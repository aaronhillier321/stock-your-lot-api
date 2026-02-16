package com.stockyourlot.controller;

import com.stockyourlot.dto.AddDealershipUserRequest;
import com.stockyourlot.dto.AddRoleRequest;
import com.stockyourlot.dto.UpdateUserRequest;
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
