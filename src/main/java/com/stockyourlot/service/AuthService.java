package com.stockyourlot.service;

import com.stockyourlot.config.JwtUtil;
import com.stockyourlot.dto.*;
import com.stockyourlot.entity.Role;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.RoleRepository;
import com.stockyourlot.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                        RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResult register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return RegisterResult.conflict("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            return RegisterResult.conflict("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), passwordHash);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        String roleName = request.role() != null ? request.role().trim().toUpperCase() : "";
        if (!java.util.Set.of("BUYER", "DEALER", "ADMIN").contains(roleName)) {
            return RegisterResult.conflict("Role is required and must be one of: BUYER, DEALER, ADMIN");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " not found in database"));
        user.getRoles().add(role);
        user = userRepository.save(user);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "Registration successful"
        );
        return RegisterResult.success(response);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );
        user = (User) authentication.getPrincipal();
        User userWithDealerships = userRepository.findByIdWithDealershipUsers(user.getId()).orElse(user);
        List<String> roleNames = userWithDealerships.getRoleNames();
        List<DealershipRoleDto> dealershipRoles = userWithDealerships.getDealershipUsers().stream()
                .map(du -> new DealershipRoleDto(
                        du.getDealership().getId(),
                        du.getDealership().getName(),
                        du.getDealershipRole()))
                .toList();
        String token = jwtUtil.generateToken(userWithDealerships.getUsername(), userWithDealerships.getEmail(), roleNames);
        return new LoginResponse("Login successful", userWithDealerships.getUsername(), userWithDealerships.getEmail(),
                roleNames, dealershipRoles, token);
    }
}
