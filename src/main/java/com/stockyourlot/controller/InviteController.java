package com.stockyourlot.controller;

import com.stockyourlot.dto.AcceptInviteRequest;
import com.stockyourlot.dto.InviteRequest;
import com.stockyourlot.dto.InviteValidateResponse;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.InviteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private static final Logger log = LoggerFactory.getLogger(InviteController.class);

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    /**
     * Invite an email address to the site. Creates a pending user and sends an invite email.
     * Requires authentication (inviter is the current user, or null if API key).
     */
    @PostMapping
    public ResponseEntity<Void> createInvite(
            @Valid @RequestBody InviteRequest request,
            @AuthenticationPrincipal Object principal) {
        log.info("Create invite request: email={}, dealershipId={}, firstName={}, lastName={}, phoneNumber={}, role={}",
                request.email(), request.dealershipId(), request.firstName(), request.lastName(), request.phoneNumber(), request.role());
        User inviter = principal instanceof User ? (User) principal : null;
        inviteService.invite(
                request.email(),
                request.dealershipId(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.role(),
                inviter);
        log.info("Invite created and email sent for email={}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Validate an invite token (e.g. before showing "set password" form).
     * Public - no auth required.
     */
    @GetMapping("/validate")
    public ResponseEntity<InviteValidateResponse> validateInvite(@RequestParam String token) {
        log.info("Validate invite token request (token length={})", token != null ? token.length() : 0);
        InviteValidateResponse response = inviteService.validateToken(token);
        log.info("Validate invite result: valid={}, email={}", response.valid(), response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Accept an invite: set password and activate the account.
     * Public - no auth required.
     */
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        log.info("Accept invite request (token length={})", request.token() != null ? request.token().length() : 0);
        inviteService.acceptInvite(request.token(), request.password());
        log.info("Invite accepted successfully");
        return ResponseEntity.ok().build();
    }
}
