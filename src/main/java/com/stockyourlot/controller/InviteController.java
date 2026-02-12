package com.stockyourlot.controller;

import com.stockyourlot.dto.AcceptInviteRequest;
import com.stockyourlot.dto.InviteRequest;
import com.stockyourlot.dto.InviteValidateResponse;
import com.stockyourlot.entity.User;
import com.stockyourlot.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

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
        User inviter = principal instanceof User ? (User) principal : null;
        inviteService.invite(request.email(), request.dealershipId(), inviter);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Validate an invite token (e.g. before showing "set password" form).
     * Public - no auth required.
     */
    @GetMapping("/validate")
    public ResponseEntity<InviteValidateResponse> validateInvite(@RequestParam String token) {
        return ResponseEntity.ok(inviteService.validateToken(token));
    }

    /**
     * Accept an invite: set password and activate the account.
     * Public - no auth required.
     */
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        inviteService.acceptInvite(request.token(), request.password());
        return ResponseEntity.ok().build();
    }
}
