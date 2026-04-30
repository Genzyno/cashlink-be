# My Ledger – Android Auth API Integration

Base URL for all endpoints (use your server host in production):

```
https://<your-server>/myledger-api/
```

Example: `https://api.myledger.com/myledger-api/`

---

## 1. Send OTP (request OTP for login/register)

**Endpoint:** `POST {baseUrl}auth/send-otp`  
**Auth:** None

**Request body (JSON):**

| Field     | Type   | Required | Description                                      |
|----------|--------|----------|--------------------------------------------------|
| email    | string | Yes      | User's email (lowercase).                         |
| channel  | string | Yes      | e.g. `"email"` or `"android"`.                   |
| clientId | string | Yes      | Unique device/session id (e.g. UUID). Used to bind OTP to this client for verify. |

**Example:**

```json
{
  "email": "user@example.com",
  "channel": "android",
  "clientId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success response:** `200`

```json
{
  "statusCode": 200,
  "message": "OTP sent successfully",
  "data": null
}
```

**Error:** `400` – Email required, or channel/clientId missing, or invalid email / rate limited.

---

## 2. Verify OTP and login (or register)

**Endpoint:** `POST {baseUrl}auth/verify-otp`  
**Auth:** None

**Request body (JSON):**

| Field     | Type   | Required | Description                                                                 |
|----------|--------|----------|-----------------------------------------------------------------------------|
| email    | string | Yes      | Same email used in send-otp.                                               |
| otp      | string | Yes      | OTP code received (e.g. by email).                                         |
| channel  | string | No       | Same as send-otp (e.g. `"android"`).                                        |
| clientId | string | Yes      | Same clientId used in send-otp.                                             |
| intent   | string | No       | `"login"` (default) or `"register"`. See below.                             |

- **`intent = "login"` or omit:** User must already exist. If email is not registered → `403 User not registered`.
- **`intent = "register"`:** If email is new, user is created (Super Admin) and tokens returned. If already registered → `409 User already registered. Please login.`

**Example (login):**

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "channel": "android",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "intent": "login"
}
```

**Success response:** `200`

```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": "user-uuid",
      "name": "John Doe",
      "email": "user@example.com",
      "roleName": "Super Admin",
      "permissions": { ... },
      "permissionScopes": { ... }
    }
  }
}
```

**Error responses:**

| Status | Meaning |
|--------|---------|
| 400    | Email/OTP missing or invalid/expired OTP. |
| 403    | User not registered (use intent `register` or sign up flow). |
| 409    | User already registered (use intent `login`). |

---

## 2b. Google Sign-In (Android / Flutter) — ID token

Use this instead of the browser redirect flow (`GET /auth/google`).

**Prerequisites:** Register **debug SHA-1** and an **Android OAuth client** in Google Cloud (package `com.myledger.my_ledger_app`). Use the **Web** client ID in `requestIdToken()`. See **`docs/GOOGLE_ANDROID_SIGNIN.md`**.

**Endpoint:** `POST {baseUrl}auth/google/id-token`  
**Auth:** None

**Request body (JSON):**

| Field    | Type   | Required | Description |
|----------|--------|----------|-------------|
| idToken  | string | Yes      | JWT from Google Sign-In (`getIdToken()`), not the OAuth access token. |

**Example:**

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**Success:** `200` — same `data` shape as verify-otp (`accessToken`, `refreshToken`, `user`, …).

**Error:** `401` — Invalid token or wrong audience; fix SHA-1 / client ID per **`docs/GOOGLE_ANDROID_SIGNIN.md`**.

---

## 3. Refresh access token

**Endpoint:** `POST {baseUrl}auth/refresh`  
**Auth:** None (use refresh token in body)

**Request body (JSON):**

| Field         | Type   | Required | Description        |
|---------------|--------|----------|--------------------|
| refreshToken  | string | Yes      | Current refresh token. |

**Example:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Success response:** `200` – Same shape as verify-otp success (new `accessToken`, `refreshToken`, `expiresIn`, `user`).

**Error:** `401` – Refresh token missing or expired.

---

## 4. Get current user (me)

**Endpoint:** `GET {baseUrl}auth/me`  
**Auth:** Required – `Authorization: Bearer <accessToken>`

**Optional header:** `X-Logged-User-Id: <user-uuid>` – If sent, must match the user in the JWT.

**Success response:** `200`

```json
{
  "statusCode": 200,
  "message": "OK",
  "data": {
    "id": "user-uuid",
    "name": "John Doe",
    "email": "user@example.com",
    "roleName": "Super Admin",
    "permissions": { ... },
    "permissionScopes": { ... }
  }
}
```

**Error:** `401` – Missing or invalid token.

---

## 5. Logout

**Endpoint:** `POST {baseUrl}auth/logout`  
**Auth:** Optional but recommended – `Authorization: Bearer <accessToken>`

**Request body:** None (or empty `{}`).

**Success response:** `200`

```json
{
  "statusCode": 200,
  "message": "Logged out",
  "data": null
}
```

**Android flow:** Call this (with current access token if available), then **discard both access and refresh tokens** on the device (clear stored tokens). There is no server-side token blacklist; logout is client-side token discard.

---

## 6. Forgot password

**Endpoint:** `POST {baseUrl}auth/forgot-password`  
**Auth:** None

**Request body (JSON):**

| Field  | Type   | Required | Description |
|--------|--------|----------|-------------|
| email  | string | Yes      | User's email. |

**Example:**

```json
{
  "email": "user@example.com"
}
```

**Success response:** `200` – Message says reset instructions sent if account exists (backend does not reveal whether email exists).

```json
{
  "statusCode": 200,
  "message": "If an account exists, we've sent reset instructions to your email.",
  "data": null
}
```

**Error:** `400` – Email missing or rate limited.

---

## 7. Send verification email (optional)

**Endpoint:** `POST {baseUrl}auth/send-verification-email`  
**Auth:** None

**Request body (JSON):** `{ "email": "user@example.com" }`

**Success:** `200` – Verification email sent.

---

## 8. Verify email (optional)

**Endpoint:** `POST {baseUrl}auth/verify-email`  
**Auth:** None

**Request body (JSON):** `{ "token": "<token-from-verification-link>" }`

**Success:** `200` – Email verified.

---

## Auth header for protected APIs

After login or refresh, send the access token on every request:

```
Authorization: Bearer <accessToken>
```

Optional: for user-scoped APIs you can send:

```
X-Logged-User-Id: <user-uuid>
```

The server will validate that this matches the user in the JWT.

---

## Recommended Android flow

1. **Login**
   - Call `POST auth/send-otp` with `email`, `channel: "android"`, `clientId` (e.g. device/session UUID).
   - User enters OTP from email.
   - Call `POST auth/verify-otp` with `email`, `otp`, `clientId`, `intent: "login"` (or `"register"` for first-time).
   - Store `data.accessToken`, `data.refreshToken`, `data.expiresIn`, `data.user` (e.g. in SharedPreferences / DataStore / encrypted storage).

2. **API calls**
   - Add header `Authorization: Bearer <accessToken>` to all requests except auth endpoints above.

3. **Refresh**
   - When access token expires (e.g. 401), call `POST auth/refresh` with `refreshToken`; store new tokens.

4. **Logout**
   - Call `POST auth/logout` with `Authorization: Bearer <accessToken>` (optional).
   - Clear stored access and refresh tokens on the device.

5. **Current user**
   - Call `GET auth/me` with Bearer token to get latest user info and permissions.

---

## Summary table

| Action           | Method | Endpoint                    | Auth   | Body (main fields)                          |
|------------------|--------|-----------------------------|--------|--------------------------------------------|
| Send OTP         | POST   | auth/send-otp               | No     | email, channel, clientId                   |
| Verify OTP       | POST   | auth/verify-otp             | No     | email, otp, clientId, intent (login/register) |
| Google (Android) | POST   | auth/google/id-token        | No     | idToken                                    |
| Refresh token    | POST   | auth/refresh                | No     | refreshToken                               |
| Get current user | GET    | auth/me                     | Bearer | —                                          |
| Logout           | POST   | auth/logout                 | Optional Bearer | —                                  |
| Forgot password  | POST   | auth/forgot-password        | No     | email                                      |
| Send verify email| POST   | auth/send-verification-email | No   | email                                      |
| Verify email     | POST   | auth/verify-email           | No     | token                                      |

All JSON responses (except 401 from filter) use the wrapper:

```json
{
  "statusCode": 200,
  "message": "...",
  "data": { ... }
}
```

Errors use same wrapper with `statusCode` 4xx/5xx and `message` describing the error; `data` is usually `null`.
