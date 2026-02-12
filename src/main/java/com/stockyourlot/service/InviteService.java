package com.stockyourlot.service;

import com.stockyourlot.config.InviteTokenUtil;
import com.stockyourlot.dto.InviteValidateResponse;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.Invite;
import com.stockyourlot.entity.InviteStatus;
import com.stockyourlot.entity.Role;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.DealershipRepository;
import com.stockyourlot.repository.InviteRepository;
import com.stockyourlot.repository.RoleRepository;
import com.stockyourlot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InviteService {

    private static final int INVITE_VALID_DAYS = 7;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealershipRepository dealershipRepository;
    private final InviteRepository inviteRepository;
    private final InviteTokenUtil inviteTokenUtil;
    private final InviteEmailService inviteEmailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${app.invite.base-url:http://localhost:8080}")
    private String baseUrl;

    public InviteService(UserRepository userRepository,
                         RoleRepository roleRepository,
                         DealershipRepository dealershipRepository,
                         InviteRepository inviteRepository,
                         InviteTokenUtil inviteTokenUtil,
                         InviteEmailService inviteEmailService,
                         org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                         UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dealershipRepository = dealershipRepository;
        this.inviteRepository = inviteRepository;
        this.inviteTokenUtil = inviteTokenUtil;
        this.inviteEmailService = inviteEmailService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Invite an email to a specific dealership. Creates a PENDING user if needed, creates an invite token, sends email.
     *
     * @param email        the email to invite
     * @param dealershipId the dealership the invite is for
     * @param inviter      the user sending the invite (can be null for system invites)
     */
    @Transactional
    public void invite(String email, UUID dealershipId, User inviter) {
        email = email != null ? email.trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        Dealership dealership = dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId));

        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email is already registered");
        }

        User user;
        if (existing != null && "PENDING".equals(existing.getStatus())) {
            user = existing;
            // Expire any existing pending invites for this email
            inviteRepository.findByEmailAndStatus(email, InviteStatus.PENDING).forEach(inv -> {
                inv.setStatus(InviteStatus.EXPIRED);
                inviteRepository.save(inv);
            });
        } else {
            user = new User(email, email, null, "PENDING");
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Default role USER not found"));
            user.getRoles().add(userRole);
            user = userRepository.save(user);
        }

        String rawToken = inviteTokenUtil.generateToken();
        String tokenHash = inviteTokenUtil.hashToken(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(INVITE_VALID_DAYS * 24L * 3600);

        Invite invite = new Invite(email, tokenHash, inviter, user, dealership, expiresAt);
        inviteRepository.save(invite);

        String acceptUrl = baseUrl + "/accept-invite?token=" + rawToken;
        inviteEmailService.sendInviteEmail(email, acceptUrl);
    }

    /**
     * Validate an invite token (e.g. for showing the "set password" form). Returns email if valid.
     */
    @Transactional(readOnly = true)
    public InviteValidateResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            return InviteValidateResponse.invalid("Token is required");
        }
        String hash = inviteTokenUtil.hashToken(token.trim());
        Invite invite = inviteRepository.findByTokenHash(hash).orElse(null);
        if (invite == null) {
            return InviteValidateResponse.invalid("Invalid or unknown invite link");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            return InviteValidateResponse.invalid("This invite has already been used");
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            return InviteValidateResponse.invalid("This invite link has expired");
        }
        Dealership d = invite.getDealership();
        return InviteValidateResponse.valid(invite.getEmail(), d.getId(), d.getName());
    }

    /**
     * Accept an invite: set password and activate the user.
     */
    @Transactional
    public void acceptInvite(String token, String password) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        String hash = inviteTokenUtil.hashToken(token.trim());
        Invite invite = inviteRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or unknown invite link"));
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite has already been used");
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite link has expired");
        }

        User user = invite.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        // Add the user to the invite's dealership as ASSOCIATE
        userService.addUserToDealership(user.getEmail(), invite.getDealership().getId(), "ASSOCIATE");
    }
}
