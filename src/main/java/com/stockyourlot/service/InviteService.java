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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(InviteService.class);
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
        log.debug("invite: email={}, dealershipId={}, inviter={}", email, dealershipId, inviter != null ? inviter.getEmail() : "null");
        email = email != null ? email.trim().toLowerCase() : "";
        if (email.isEmpty()) {
            log.warn("invite: rejected - empty email");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        Dealership dealership = dealershipRepository.findById(dealershipId)
                .orElseThrow(() -> {
                    log.warn("invite: dealership not found, id={}", dealershipId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealership not found: " + dealershipId);
                });
        log.debug("invite: dealership found id={}, name={}", dealership.getId(), dealership.getName());

        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            log.warn("invite: rejected - user already active, email={}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email is already registered");
        }

        User user;
        if (existing != null && "PENDING".equals(existing.getStatus())) {
            user = existing;
            log.debug("invite: reusing pending user id={}", user.getId());
            List<Invite> expired = inviteRepository.findByEmailAndStatus(email, InviteStatus.PENDING);
            if (!expired.isEmpty()) {
                log.info("invite: expiring {} existing pending invite(s) for email={}", expired.size(), email);
                expired.forEach(inv -> {
                    inv.setStatus(InviteStatus.EXPIRED);
                    inviteRepository.save(inv);
                });
            }
        } else {
            user = new User(email, null, "PENDING");
            Role userRole = roleRepository.findByName("BUYER")
                    .orElseThrow(() -> new IllegalStateException("Role BUYER not found"));
            user.getRoles().add(userRole);
            user = userRepository.save(user);
            log.info("invite: created pending user id={} for email={}", user.getId(), email);
        }

        String rawToken = inviteTokenUtil.generateToken();
        String tokenHash = inviteTokenUtil.hashToken(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(INVITE_VALID_DAYS * 24L * 3600);

        Invite invite = new Invite(email, tokenHash, inviter, user, dealership, expiresAt);
        inviteRepository.save(invite);
        log.info("invite: created invite id={} for email={}, dealershipId={}, expiresAt={}", invite.getId(), email, dealershipId, expiresAt);

        String acceptUrl = baseUrl + "/accept-invite?token=" + rawToken;
        log.debug("invite: sending email to {}", email);
        inviteEmailService.sendInviteEmail(email, acceptUrl);
        log.info("invite: completed for email={}", email);
    }

    /**
     * Validate an invite token (e.g. for showing the "set password" form). Returns email if valid.
     */
    @Transactional(readOnly = true)
    public InviteValidateResponse validateToken(String token) {
        log.debug("validateToken: checking token");
        if (token == null || token.isBlank()) {
            log.info("validateToken: invalid - token missing");
            return InviteValidateResponse.invalid("Token is required");
        }
        String hash = inviteTokenUtil.hashToken(token.trim());
        Invite invite = inviteRepository.findByTokenHash(hash).orElse(null);
        if (invite == null) {
            log.info("validateToken: invalid - no invite found for token");
            return InviteValidateResponse.invalid("Invalid or unknown invite link");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            log.info("validateToken: invalid - invite not pending, status={}, inviteId={}", invite.getStatus(), invite.getId());
            return InviteValidateResponse.invalid("This invite has already been used");
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            log.info("validateToken: invalid - invite expired, inviteId={}, expiresAt={}", invite.getId(), invite.getExpiresAt());
            return InviteValidateResponse.invalid("This invite link has expired");
        }
        Dealership d = invite.getDealership();
        log.info("validateToken: valid for email={}, dealershipId={}, dealershipName={}", invite.getEmail(), d.getId(), d.getName());
        return InviteValidateResponse.valid(invite.getEmail(), d.getId(), d.getName());
    }

    /**
     * Accept an invite: set password and activate the user.
     */
    @Transactional
    public void acceptInvite(String token, String password) {
        log.debug("acceptInvite: processing request");
        if (token == null || token.isBlank()) {
            log.warn("acceptInvite: rejected - token missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        String hash = inviteTokenUtil.hashToken(token.trim());
        Invite invite = inviteRepository.findByTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("acceptInvite: invalid - no invite found for token");
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or unknown invite link");
                });
        log.debug("acceptInvite: invite found id={}, email={}", invite.getId(), invite.getEmail());
        if (invite.getStatus() != InviteStatus.PENDING) {
            log.warn("acceptInvite: rejected - invite not pending, status={}, inviteId={}", invite.getStatus(), invite.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite has already been used");
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            log.warn("acceptInvite: rejected - invite expired, inviteId={}", invite.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite link has expired");
        }

        User user = invite.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus("ACTIVE");
        userRepository.save(user);
        log.info("acceptInvite: activated user id={}, email={}", user.getId(), user.getEmail());

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        log.debug("acceptInvite: marked invite id={} as ACCEPTED", invite.getId());

        userService.addUserToDealership(user.getEmail(), invite.getDealership().getId(), "ASSOCIATE");
        log.info("acceptInvite: added user to dealership id={}, invite flow complete for email={}", invite.getDealership().getId(), user.getEmail());
    }
}
