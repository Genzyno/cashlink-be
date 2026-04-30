package com.john.ledger.entry.dto.response;

import lombok.*;

import java.util.UUID;

/** Invite details for the accept-invite UI (e.g. show role and email before user sets password). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteDetailsResponse {

    private String email;
    private UUID roleId;
    private String roleName;
    private boolean valid;
    private String message;
}
