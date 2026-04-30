package com.john.ledger.auth.dto;

import lombok.Data;

/** Android / Flutter: send the Google Sign-In ID token from the client. */
@Data
public class AuthGoogleIdTokenRequest {
    /** JWT id_token from Google Sign-In (not access_token). */
    private String idToken;
}
