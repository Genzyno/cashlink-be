package com.john.ledger.entry.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectInviteRequest {

    /** Token from the invite email link (Reject button). */
    private String token;
}
