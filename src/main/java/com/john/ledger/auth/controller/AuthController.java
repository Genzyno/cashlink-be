package com.john.ledger.auth.controller;

import com.john.ledger.auth.dto.LedgerAuthTokenResponse;
import com.john.ledger.auth.dto.AuthRefreshRequest;
import com.john.ledger.auth.dto.AuthLoginRequest;
import com.john.ledger.auth.dto.AuthSendOtpRequest;
import com.john.ledger.auth.dto.AuthVerifyOtpRequest;
import com.john.ledger.auth.dto.AuthForgotPasswordRequest;
import com.john.ledger.auth.dto.AuthResetPasswordRequest;
import com.john.ledger.auth.dto.AuthSendVerificationEmailRequest;
import com.john.ledger.auth.dto.AuthGoogleIdTokenRequest;
import com.john.ledger.auth.dto.AuthVerifyEmailRequest;
import com.john.ledger.auth.dto.GoogleCallbackResult;
import com.john.ledger.auth.dto.LedgerAuthUserInfo;
import com.john.ledger.auth.dto.LoginResult;
import com.john.ledger.auth.dto.SendOtpResult;
import com.john.ledger.auth.dto.VerifyOtpResult;
import com.john.ledger.auth.service.AuthService;
import com.john.ledger.common.util.CurrentUserHolder;
import com.john.ledger.common.util.ServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ServiceResponse<LedgerAuthTokenResponse>> login(@RequestBody AuthLoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Email and password are required"));
        }

        LoginResult result = authService.loginWithPassword(
                request.getEmail(),
                request.getPassword(),
                request.getChannel(),
                request.getClientId());

        if (result.getStatus() == LoginResult.Status.INVALID_CREDENTIALS) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "Invalid email or password"));
        }
        if (result.getStatus() == LoginResult.Status.USER_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServiceResponse.failureResponse(404, "Account not found. Please sign up first."));
        }
        if (result.getStatus() == LoginResult.Status.USER_INACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServiceResponse.failureResponse(403, "Account is inactive. Please contact support."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Login successful", result.getResponse()));
    }

    @Operation(summary = "Send OTP to email")
    @PostMapping("/send-otp")
    public ResponseEntity<ServiceResponse<Void>> sendOtp(@RequestBody AuthSendOtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Email is required"));
        }
        if (request.getChannel() == null || request.getClientId() == null || request.getClientId().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Channel and clientId are required"));
        }

        SendOtpResult result = authService.sendOtp(
                request.getEmail().toLowerCase().trim(),
                request.getChannel(),
                request.getClientId(),
                request.getIntent());

        if (result.getStatus() == SendOtpResult.Status.INVALID_EMAIL) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid email address."));
        }
        if (result.getStatus() == SendOtpResult.Status.RATE_LIMITED) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ServiceResponse.failureResponse(429, "Too many requests. Try again later."));
        }
        if (result.getStatus() == SendOtpResult.Status.USER_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServiceResponse.failureResponse(404, "Account not found. Please sign up first."));
        }
        if (result.getStatus() == SendOtpResult.Status.USER_ALREADY_EXISTS) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ServiceResponse.failureResponse(409, "Account already exists. Please login."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "OTP sent successfully", null));
    }

    @Operation(summary = "Verify OTP and login or register (intent=login|register)")
    @PostMapping("/verify-otp")
    public ResponseEntity<ServiceResponse<LedgerAuthTokenResponse>> verifyOtp(@RequestBody AuthVerifyOtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Email and OTP are required"));
        }

        VerifyOtpResult result = authService.verifyOtpAndLogin(
                request.getEmail(),
                request.getOtp(),
                request.getChannel(),
                request.getClientId(),
                request.getIntent());

        if (result.getStatus() == VerifyOtpResult.Status.INVALID_OTP) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid or expired OTP"));
        }
        if (result.getStatus() == VerifyOtpResult.Status.USER_NOT_REGISTERED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServiceResponse.failureResponse(403, "User not registered. Please sign up."));
        }
        if (result.getStatus() == VerifyOtpResult.Status.USER_ALREADY_REGISTERED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ServiceResponse.failureResponse(409, "User already registered. Please login."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Login successful", result.getResponse()));
    }

    @Operation(summary = "Logout: acknowledge sign-out. Client should discard tokens after calling. Optional: send Bearer token.")
    @PostMapping("/logout")
    public ResponseEntity<ServiceResponse<Void>> logout() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Logged out", null));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<ServiceResponse<LedgerAuthTokenResponse>> refresh(@RequestBody AuthRefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "Refresh token is required"));
        }

        LedgerAuthTokenResponse data = authService.refresh(request.getRefreshToken());

        if (data == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "Refresh token expired"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Token refreshed", data));
    }

    @Operation(summary = "Current user: return full user with permissions and permissionScopes (requires Bearer token)")
    @GetMapping("/me")
    public ResponseEntity<ServiceResponse<LedgerAuthUserInfo>> getCurrentUser() {
        Optional<UUID> userIdOpt = CurrentUserHolder.getUserId();
        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "Unauthorized"));
        }
        LedgerAuthUserInfo user = authService.getCurrentUser(userIdOpt.get());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "User not found or inactive"));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "OK", user));
    }

    @Operation(summary = "Google Sign-In (Android/Flutter): verify id_token from Google Sign-In SDK, return app JWTs")
    @PostMapping("/google/id-token")
    public ResponseEntity<ServiceResponse<LedgerAuthTokenResponse>> googleIdToken(@RequestBody AuthGoogleIdTokenRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "idToken is required"));
        }
        LedgerAuthTokenResponse data = authService.loginWithGoogleIdToken(request.getIdToken());
        if (data == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServiceResponse.failureResponse(401, "Invalid or expired Google ID token. Ensure Web Client ID is used in requestIdToken() and SHA-1 is registered in Google Cloud."));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Google sign-in successful", data));
    }

    @Operation(summary = "Google Sign-In: redirect browser to Google OAuth. redirect_uri = where to send user after sign-in (URL-encoded).")
    @GetMapping("/google")
    public ResponseEntity<Void> googleSignIn(@RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        String url = authService.buildGoogleAuthorizationUrl(redirectUri != null ? redirectUri : "");
        if (url == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(url)).build();
    }

    @Operation(summary = "Google OAuth callback: exchange code for tokens, or redirect with error=user_not_registered if not registered.")
    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam(value = "code", required = false) String code,
                                                @RequestParam(value = "state", required = false) String state) {
        if (state == null || state.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        GoogleCallbackResult callbackResult = authService.handleGoogleCallback(code, state);
        if (callbackResult.getStatus() == GoogleCallbackResult.Status.USER_NOT_REGISTERED) {
            String redirectUrl = state + (state.contains("#") ? "&" : "#") + "error=user_not_registered";
            return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(redirectUrl)).build();
        }
        if (callbackResult.getStatus() != GoogleCallbackResult.Status.SUCCESS || callbackResult.getResponse() == null) {
            String redirectUrl = state + (state.contains("#") ? "&" : "#") + "error=server_error";
            return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(redirectUrl)).build();
        }
        LedgerAuthTokenResponse data = callbackResult.getResponse();
        String fragment = "access_token=" + URLEncoder.encode(data.getAccessToken() != null ? data.getAccessToken() : "", StandardCharsets.UTF_8)
                + "&refresh_token=" + URLEncoder.encode(data.getRefreshToken() != null ? data.getRefreshToken() : "", StandardCharsets.UTF_8)
                + "&expires_in=" + (data.getExpiresIn() != null ? data.getExpiresIn() : 0)
                + "&token_type=" + URLEncoder.encode(data.getTokenType() != null ? data.getTokenType() : "Bearer", StandardCharsets.UTF_8);
        String redirectUrl = state + (state.contains("#") ? "&" : "#") + fragment;
        return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(redirectUrl)).build();
    }

    @Operation(summary = "Forgot password: send reset link to email")
    @PostMapping("/forgot-password")
    public ResponseEntity<ServiceResponse<Void>> forgotPassword(@RequestBody AuthForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Email is required"));
        }
        String email = request.getEmail().toLowerCase().trim();
        int result = authService.forgotPassword(email);
        if (result == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServiceResponse.failureResponse(404, "Account not found. Please sign up first."));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ServiceResponse.failureResponse(429, "Too many requests. Please try again later."));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "If an account exists, we've sent reset instructions to your email.", null));
    }

    @Operation(summary = "Reset password using token from reset link")
    @PostMapping("/reset-password")
    public ResponseEntity<ServiceResponse<Void>> resetPassword(@RequestBody AuthResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank() || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Token and new password are required"));
        }
        if (!authService.resetPassword(request.getToken(), request.getNewPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid or expired reset token. Please request a new password reset."));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Password reset successfully! You can now login with your new password.", null));
    }

    @Operation(summary = "Send email verification link")
    @PostMapping("/send-verification-email")
    public ResponseEntity<ServiceResponse<Void>> sendVerificationEmail(@RequestBody AuthSendVerificationEmailRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Email is required"));
        }

        if (!authService.sendVerificationEmail(request.getEmail().toLowerCase().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid email address or too many requests. Please try again later."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Verification email sent successfully. Please check your inbox.", null));
    }

    @Operation(summary = "Verify email using token from verification link")
    @PostMapping("/verify-email")
    public ResponseEntity<ServiceResponse<Void>> verifyEmail(@RequestBody AuthVerifyEmailRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Verification token is required"));
        }

        if (!authService.verifyEmailToken(request.getToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid or expired verification token. Please request a new verification email."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Email verified successfully!", null));
    }

    @Operation(summary = "Verify email using token from URL (GET endpoint for direct link clicks)")
    @GetMapping("/verify-email")
    public ResponseEntity<ServiceResponse<Void>> verifyEmailGet(@RequestParam String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Verification token is required"));
        }

        if (!authService.verifyEmailToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid or expired verification token. Please request a new verification email."));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ServiceResponse.successResponse(200, "Email verified successfully!", null));
    }
}
