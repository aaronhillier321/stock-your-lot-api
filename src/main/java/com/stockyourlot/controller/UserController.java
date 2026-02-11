package com.stockyourlot.controller;

import com.stockyourlot.dto.AddDealershipUserRequest;
import com.stockyourlot.dto.UserRolesResponse;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
