package com.stockyourlot.controller;

import com.stockyourlot.dto.AddDealershipUserRequest;
import com.stockyourlot.dto.AddRoleRequest;
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
     * Add a global role (USER, SALES_ASSOCIATE, SALES_ADMIN) to a user.
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
     * Add a user to a dealership with an optional role (ASSOCIATE or ADMIN).
     * Defaults to ASSOCIATE if role is not provided.
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
     * Remove a user completely. Deletes the user and all related data (roles, dealership
     * memberships, invites where they are the invited user). Requires authentication.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
