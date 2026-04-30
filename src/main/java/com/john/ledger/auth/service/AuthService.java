package com.john.ledger.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.john.ledger.auth.dto.LedgerAuthTokenResponse;
import com.john.ledger.auth.dto.LedgerAuthUserInfo;
import com.john.ledger.auth.dto.GoogleCallbackResult;
import com.john.ledger.auth.dto.VerifyOtpResult;
import com.john.ledger.auth.entity.PasswordResetTokenEntity;
import com.john.ledger.auth.repository.PasswordResetTokenRepository;
import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.entity.UserEntity;
import com.john.ledger.entry.repository.RoleRepository;
import com.john.ledger.entry.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    @Autowired
    private OtpService otpService;
    @Autowired
    private OtpEmailService otpEmailService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private EmailVerificationEmailService emailVerificationEmailService;
    @Autowired
    private RoleRepository roleRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PASSWORD_RESET_EXPIRATION_HOURS = 1;
    private static final int PASSWORD_RESET_RATE_LIMIT_PER_HOUR = 3;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.google.client-id:}")
    private String googleClientId;
    @Value("${app.google.client-secret:}")
    private String googleClientSecret;
    @Value("${app.google.redirect-uri:}")
    private String googleRedirectUri;
    /** Comma-separated extra OAuth client IDs accepted as id_token audience (e.g. Android client ID). */
    @Value("${app.google.additional-audiences:}")
    private String googleAdditionalAudiences;

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    public boolean sendOtp(String email, String channel, String clientId) {
        if (email == null || email.isBlank()) return false;
        email = email.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;
        if (channel == null || channel.isBlank() || clientId == null || clientId.isBlank()) return false;

        String otp = otpService.generateAndStore(email, channel, clientId);
        if (otp == null) return false; // rate limited
        // Send email asynchronously - don't wait for it
        otpEmailService.sendOtpToEmail(email, otp);
        return true;
    }

    private static final String SUPER_ADMIN_ROLE_NAME = "Super Admin";

    /**
     * Verify OTP and login or register.
     * intent=login (or omit): do not create user; return USER_NOT_REGISTERED if email not in DB.
     * intent=register: create user with Super Admin role if new, then return tokens. No business created.
     */
    public VerifyOtpResult verifyOtpAndLogin(String email, String otp, String channel, String clientId, String intent) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) return VerifyOtpResult.invalidOtp();
        email = email.toLowerCase().trim();
        if (!otpService.consumeOtp(email, otp, clientId)) return VerifyOtpResult.invalidOtp();

        boolean isRegister = "register".equalsIgnoreCase(intent != null ? intent.trim() : "");

        Optional<UserEntity> userOpt = userRepository.findByUserEmailWithRole(email);
        UserEntity user;

        if (userOpt.isEmpty()) {
            if (isRegister) {
                user = createUserWithSuperAdminRole(email);
                if (user == null) return VerifyOtpResult.invalidOtp();
            } else {
                return VerifyOtpResult.userNotRegistered();
            }
        } else {
            if (isRegister) {
                return VerifyOtpResult.userAlreadyRegistered();
            }
            user = userOpt.get();
            if (!Boolean.TRUE.equals(user.getStatus())) return VerifyOtpResult.invalidOtp();
        }

        String userId = user.getId().toString();
        String name = user.getUserName() != null ? user.getUserName() : "";
        String accessToken = jwtService.createAccessToken(userId, user.getUserEmail(), name);
        String refreshToken = jwtService.createRefreshToken(userId, user.getUserEmail());

        String roleName = null;
        Map<String, List<String>> permissions = null;
        Map<String, Map<String, String>> permissionScopes = null;
        if (user.getRoleEntity() != null) {
            roleName = user.getRoleEntity().getRoleName();
            permissions = user.getRoleEntity().getPermissions();
            permissionScopes = user.getRoleEntity().getPermissionScopes();
        }
        if (permissionScopes == null) permissionScopes = Collections.emptyMap();

        LedgerAuthUserInfo userInfo = LedgerAuthUserInfo.builder()
                .id(userId)
                .name(name)
                .email(com.john.ledger.common.util.EmailMasker.mask(user.getUserEmail()))
                .roleName(roleName)
                .permissions(permissions)
                .permissionScopes(permissionScopes)
                .build();

        LedgerAuthTokenResponse response = LedgerAuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationSec())
                .tokenType("Bearer")
                .user(userInfo)
                .build();
        return VerifyOtpResult.success(response);
    }

    /** Create a new user with Super Admin role. No business created. */
    private UserEntity createUserWithSuperAdminRole(String email) {
        Optional<RoleEntity> roleOpt = roleRepository.findByRoleName(SUPER_ADMIN_ROLE_NAME);
        if (roleOpt.isEmpty()) return null;
        int at = email.indexOf('@');
        String name = (at > 0 ? email.substring(0, Math.min(at, 50)) : email).trim();
        if (name.isBlank() || name.length() < 3) name = "User";
        String mobile = "R" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String password = generateSecureToken();
        UserEntity user = UserEntity.builder()
                .userName(name)
                .userEmail(email)
                .userMobile(mobile)
                .password(password)
                .roleEntity(roleOpt.get())
                .status(true)
                .build();
        return userRepository.save(user);
    }

    public LedgerAuthTokenResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return null;
        JwtService.TokenPayload payload = jwtService.parseRefreshToken(refreshToken);
        if (payload == null) return null;

        Optional<UserEntity> userOpt = userRepository.findByIdWithRole(java.util.UUID.fromString(payload.userId()));
        if (userOpt.isEmpty() || !Boolean.TRUE.equals(userOpt.get().getStatus())) return null;

        UserEntity user = userOpt.get();
        String accessToken = jwtService.createAccessToken(user.getId().toString(), user.getUserEmail(), user.getUserName());
        String newRefreshToken = jwtService.createRefreshToken(user.getId().toString(), user.getUserEmail());

        String roleName = null;
        Map<String, List<String>> permissions = null;
        Map<String, Map<String, String>> permissionScopes = null;
        if (user.getRoleEntity() != null) {
            roleName = user.getRoleEntity().getRoleName();
            permissions = user.getRoleEntity().getPermissions();
            permissionScopes = user.getRoleEntity().getPermissionScopes();
        }
        if (permissionScopes == null) permissionScopes = Collections.emptyMap();
        LedgerAuthUserInfo userInfo = LedgerAuthUserInfo.builder()
                .id(user.getId().toString())
                .name(user.getUserName() != null ? user.getUserName() : "")
                .email(com.john.ledger.common.util.EmailMasker.mask(user.getUserEmail()))
                .roleName(roleName)
                .permissions(permissions)
                .permissionScopes(permissionScopes)
                .build();

        return LedgerAuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessExpirationSec())
                .tokenType("Bearer")
                .user(userInfo)
                .build();
    }

    /**
     * Load current user by ID with role and permissions (for GET /auth/me).
     */
    public LedgerAuthUserInfo getCurrentUser(java.util.UUID userId) {
        if (userId == null) return null;
        Optional<UserEntity> userOpt = userRepository.findByIdWithRole(userId);
        if (userOpt.isEmpty() || !Boolean.TRUE.equals(userOpt.get().getStatus())) return null;
        UserEntity user = userOpt.get();
        String roleName = null;
        Map<String, List<String>> permissions = null;
        Map<String, Map<String, String>> permissionScopes = null;
        if (user.getRoleEntity() != null) {
            roleName = user.getRoleEntity().getRoleName();
            permissions = user.getRoleEntity().getPermissions();
            permissionScopes = user.getRoleEntity().getPermissionScopes();
        }
        if (permissionScopes == null) permissionScopes = Collections.emptyMap();
        return LedgerAuthUserInfo.builder()
                .id(user.getId().toString())
                .name(user.getUserName() != null ? user.getUserName() : "")
                .email(com.john.ledger.common.util.EmailMasker.mask(user.getUserEmail()))
                .roleName(roleName)
                .permissions(permissions)
                .permissionScopes(permissionScopes)
                .build();
    }

    /**
     * Forgot password: send reset link to email. Always returns true for valid email format to avoid email enumeration.
     * Rate limited per email. Link expires in 1 hour.
     */
    @Transactional
    public boolean forgotPassword(String email) {
        if (email == null || email.isBlank()) return false;
        email = email.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long count = passwordResetTokenRepository.countByEmailSince(email, since);
        if (count >= PASSWORD_RESET_RATE_LIMIT_PER_HOUR) return false;

        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(PASSWORD_RESET_EXPIRATION_HOURS);
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .email(email)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        passwordResetTokenRepository.save(entity);
        emailVerificationEmailService.sendPasswordResetEmail(email, token, frontendUrl);
        return true;
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Send email verification link to user's email address
     * Email is sent asynchronously in the background
     */
    public boolean sendVerificationEmail(String email) {
        if (email == null || email.isBlank()) return false;
        email = email.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;

        // Send verification email asynchronously - don't wait for it
        return emailVerificationService.generateAndSendVerificationToken(email, frontendUrl);
    }

    /**
     * Verify email token and return verification status
     */
    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) return false;
        return emailVerificationService.verifyToken(token);
    }

    /**
     * Check if an email is verified
     */
    public boolean isEmailVerified(String email) {
        if (email == null || email.isBlank()) return false;
        email = email.toLowerCase().trim();
        return emailVerificationService.isEmailVerified(email);
    }

    /**
     * Build Google OAuth2 authorization URL. Redirect the user's browser here.
     * @param redirectUriFrontend URL-encoded frontend redirect (where to send user with tokens after Google sign-in)
     */
    public String buildGoogleAuthorizationUrl(String redirectUriFrontend) {
        if (googleClientId == null || googleClientId.isBlank() || googleRedirectUri == null || googleRedirectUri.isBlank()) {
            return null;
        }
        String state = redirectUriFrontend != null ? redirectUriFrontend : "";
        String scope = "openid email profile";
        return GOOGLE_AUTH_URL
                + "?client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&access_type=offline&prompt=consent";
    }

    /**
     * Exchange Google authorization code for tokens, get user info. If user exists, issue JWT.
     * If user is new, create user with Super Admin role (no business) and issue JWT.
     */
    public GoogleCallbackResult handleGoogleCallback(String code, String state) {
        if (code == null || code.isBlank() || googleClientId == null || googleClientId.isBlank()
                || googleClientSecret == null || googleClientSecret.isBlank() || googleRedirectUri == null || googleRedirectUri.isBlank()) {
            return GoogleCallbackResult.error();
        }
        try {
            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("client_id", googleClientId);
            tokenRequest.add("client_secret", googleClientSecret);
            tokenRequest.add("code", code.trim());
            tokenRequest.add("grant_type", "authorization_code");
            tokenRequest.add("redirect_uri", googleRedirectUri);
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<Map<String, Object>> tokenResponse = REST_TEMPLATE.exchange(
                    GOOGLE_TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(tokenRequest, tokenHeaders),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) return GoogleCallbackResult.error();
            Map<String, Object> tokenBody = tokenResponse.getBody();
            Object accessTokenObj = tokenBody.get("access_token");
            if (accessTokenObj == null) return GoogleCallbackResult.error();
            String googleAccessToken = accessTokenObj.toString();

            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(googleAccessToken);
            ResponseEntity<Map<String, Object>> userInfoResponse = REST_TEMPLATE.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(userInfoHeaders),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) return GoogleCallbackResult.error();
            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = userInfo.get("email") != null ? userInfo.get("email").toString().toLowerCase().trim() : null;
            if (email == null || email.isBlank()) return GoogleCallbackResult.error();
            String name = userInfo.get("name") != null ? userInfo.get("name").toString().trim() : email;

            Optional<UserEntity> userOpt = userRepository.findByUserEmailWithRole(email);
            UserEntity user;
            if (userOpt.isEmpty()) {
                UserEntity created = createUserFromGoogle(email, name);
                if (created == null) return GoogleCallbackResult.error();
                user = userRepository.findByUserEmailWithRole(email).orElse(created);
            } else {
                user = userOpt.get();
                if (!Boolean.TRUE.equals(user.getStatus())) return GoogleCallbackResult.error();
            }

            return GoogleCallbackResult.success(buildLedgerAuthResponseForGoogleUser(user));
        } catch (Exception e) {
            return GoogleCallbackResult.error();
        }
    }

    /**
     * Android / Flutter: verify Google Sign-In ID token and return app JWTs.
     * Configure SHA-1 + Android OAuth client in Google Cloud; use Web client ID in requestIdToken().
     */
    public LedgerAuthTokenResponse loginWithGoogleIdToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank() || googleClientId == null || googleClientId.isBlank()) {
            return null;
        }
        try {
            List<String> audiences = new ArrayList<>();
            audiences.add(googleClientId.trim());
            if (googleAdditionalAudiences != null && !googleAdditionalAudiences.isBlank()) {
                for (String part : googleAdditionalAudiences.split(",")) {
                    String p = part.trim();
                    if (!p.isEmpty() && !audiences.contains(p)) {
                        audiences.add(p);
                    }
                }
            }
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(audiences)
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString.trim());
            if (idToken == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail() != null ? payload.getEmail().toLowerCase().trim() : null;
            if (email == null || email.isBlank()) {
                return null;
            }
            if (payload.getEmailVerified() != null && Boolean.FALSE.equals(payload.getEmailVerified())) {
                return null;
            }
            String name = payload.get("name") != null ? payload.get("name").toString().trim() : email;

            Optional<UserEntity> userOpt = userRepository.findByUserEmailWithRole(email);
            UserEntity user;
            if (userOpt.isEmpty()) {
                UserEntity created = createUserFromGoogle(email, name);
                if (created == null) {
                    return null;
                }
                user = userRepository.findByUserEmailWithRole(email).orElse(created);
            } else {
                user = userOpt.get();
                if (!Boolean.TRUE.equals(user.getStatus())) {
                    return null;
                }
            }
            return buildLedgerAuthResponseForGoogleUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private LedgerAuthTokenResponse buildLedgerAuthResponseForGoogleUser(UserEntity user) {
        String userId = user.getId().toString();
        String accessToken = jwtService.createAccessToken(userId, user.getUserEmail(), user.getUserName());
        String refreshToken = jwtService.createRefreshToken(userId, user.getUserEmail());
        String roleName = null;
        Map<String, List<String>> permissions = null;
        Map<String, Map<String, String>> permissionScopes = null;
        if (user.getRoleEntity() != null) {
            roleName = user.getRoleEntity().getRoleName();
            permissions = user.getRoleEntity().getPermissions();
            permissionScopes = user.getRoleEntity().getPermissionScopes();
        }
        if (permissionScopes == null) {
            permissionScopes = Collections.emptyMap();
        }
        LedgerAuthUserInfo userInfoDto = LedgerAuthUserInfo.builder()
                .id(userId)
                .name(user.getUserName() != null ? user.getUserName() : "")
                .email(com.john.ledger.common.util.EmailMasker.mask(user.getUserEmail()))
                .roleName(roleName)
                .permissions(permissions)
                .permissionScopes(permissionScopes)
                .build();
        return LedgerAuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationSec())
                .tokenType("Bearer")
                .user(userInfoDto)
                .build();
    }

    /** Create a new user from Google sign-in with Super Admin role. No business created. */
    private UserEntity createUserFromGoogle(String email, String name) {
        Optional<RoleEntity> roleOpt = roleRepository.findByRoleName(SUPER_ADMIN_ROLE_NAME);
        if (roleOpt.isEmpty()) return null;
        String userName = (name != null && !name.isBlank()) ? name.trim() : email;
        if (userName.length() > 50) userName = userName.substring(0, 50);
        if (userName.length() < 3) userName = "User";
        String mobile = "G" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String password = generateSecureToken();
        UserEntity user = UserEntity.builder()
                .userName(userName)
                .userEmail(email)
                .userMobile(mobile)
                .password(password)
                .roleEntity(roleOpt.get())
                .status(true)
                .build();
        return userRepository.save(user);
    }
}
