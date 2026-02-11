package com.stockyourlot.controller;

import com.stockyourlot.dto.SetUserRolesRequest;
import com.stockyourlot.dto.UserRolesResponse;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/roles")
    public ResponseEntity<UserRolesResponse> setUserRoles(
            @Valid @RequestBody SetUserRolesRequest request) {
        User user = userService.setUserRoles(request.email(), request.roles());
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .toList();
        return ResponseEntity.ok(new UserRolesResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleNames
        ));
    }
}
