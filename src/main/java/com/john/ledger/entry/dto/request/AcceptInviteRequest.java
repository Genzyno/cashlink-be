package com.john.ledger.entry.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptInviteRequest {

    /** Token from the invite email link. */
    private String token;
    /** Display name for the new user. */
    private String userName;
    /** Password to set for the new account. */
    private String password;
    /** Mobile number (optional; use "Pending" if not provided). */
    private String userMobile;
}
