# Auth API – Endpoints and Payloads

Base path: `{apiUrl}/auth` (e.g. `http://localhost:8080/myledger-api/auth`).

**Request headers (all API calls when user is logged in):**  
The frontend should send the logged-in user ID on every authenticated request so the backend can scope data by user. Use these headers for all endpoints (except auth endpoints that use `X-Skip-Auth` or do not require auth):

| Header               | Description                          |
|----------------------|--------------------------------------|
| `Authorization`      | `Bearer <access_token>`              |
| `X-Logged-User-Id`    | Current user's ID (UUID). Backend uses the user ID from the JWT for filtering; when this header is sent, the backend validates that it matches the token and returns 403 if it does not. |

All POST responses use the wrapper below unless noted.

**Response wrapper (all POST endpoints):**
```json
{
  "data": { ... },
  "message": "string",
  "statusCode": 200
}
```

---

## 1. Send OTP (Login / Sign-up)

**Endpoint:** `POST /auth/send-otp`

**Payload:**
```json
{
  "email": "user@example.com",
  "channel": "WEB",
  "clientId": "web-<uuid>"
}
```

| Field     | Type   | Required | Description                          |
|----------|--------|----------|--------------------------------------|
| email    | string | Yes      | User email (trimmed, lowercased)    |
| channel  | string | Yes      | Always `"WEB"`                       |
| clientId | string | Yes      | Session/device id (e.g. `web-<uuid>`) |

**Success response:** `statusCode: 200` or `201`, `data` can be empty or contain metadata. Frontend shows "OTP sent" and moves to OTP step.

---

## 2. Verify OTP (Sign in / Create account)

**Endpoint:** `POST /auth/verify-otp`

**Payload:**
```json
{
  "email": "user@example.com",
  "otp": "1234",
  "channel": "WEB",
  "clientId": "web-<uuid>",
  "intent": "login"
}
```

| Field     | Type   | Required | Description                |
|----------|--------|----------|----------------------------|
| email    | string | Yes      | Same email used in send-otp |
| otp      | string | Yes      | 4-digit OTP                |
| channel  | string | Yes      | Always `"WEB"`              |
| clientId | string | Yes      | Same clientId as send-otp   |
| intent   | string | No       | `"login"` or `"register"`. **login** (or omit): do not create user; if email not registered return 403. **register**: create user with **Super Admin** role if new, then return tokens. Default = login. No business is created for the new user. |

**When user is not registered (sign-in, intent = `"login"` or omitted):** Backend returns **403** with message e.g. `"User not registered. Please sign up."`. Frontend shows "Account not found. Please sign up first." and link to sign-up.

**When user is already registered (sign-up, intent = `"register"`):** Backend returns **409** with message `"User already registered. Please login."`. Frontend should show this message and direct the user to the login flow.

**When user is new (sign-up, intent = `"register"`):** Backend creates the user, assigns **Super Admin** role (full permissions), then returns the same success response (tokens + user). **Do not create any demo business or demo book** for the new user—only the user record. Frontend will show only "Create new business" in the header and on the dashboard until the user creates one.

**Success response:** `statusCode: 200` or `201`, with `data` containing tokens and user:

```json
{
  "data": {
    "accessToken": "string",
    "refreshToken": "string",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": "string",
      "name": "string",
      "email": "string",
      "roleName": "string",
      "permissions": {
        "business": ["view", "create"],
        "book": ["view", "read"],
        "user": ["view"],
        "role": ["view"],
        "transaction": ["view", "create", "update", "delete"]
      },
      "permissionScopes": {
        "business": { "view": "all" },
        "book": { "view": "assigned" }
      }
    }
  },
  "message": "string",
  "statusCode": 200
}
```

| Field (data)     | Type   | Description                                      |
|------------------|--------|--------------------------------------------------|
| accessToken      | string | JWT for `Authorization: Bearer`                 |
| refreshToken     | string | Used to get new access token                    |
| expiresIn        | number | Access token TTL in seconds                     |
| tokenType        | string | Optional; default `"Bearer"`                     |
| user.id          | string | User ID                                          |
| user.name        | string | Display name                                     |
| user.email       | string | Email                                            |
| user.roleName    | string | Role display name                                |
| user.permissions | object | Resource → list of actions (e.g. view, create)  |
| user.permissionScopes | object | Optional; screen → action → `"all"` or `"assigned"` |

---

## 3. Refresh Token

**Endpoint:** `POST /auth/refresh`

**Headers:** `X-Skip-Auth: true` (no Bearer token) if your client sends it; otherwise omit Authorization.

**Payload:**
```json
{
  "refreshToken": "string"
}
```

**Success response:** Same shape as Verify OTP (`data.accessToken`, `data.refreshToken`, `data.user`). Frontend replaces stored tokens and user.

---

## 4. Forgot Password

**Endpoint:** `POST /auth/forgot-password`

**Payload:**
```json
{
  "email": "user@example.com"
}
```

| Field | Type   | Required | Description                |
|-------|--------|----------|----------------------------|
| email | string | Yes      | Trimmed, lowercased email  |

**Success response:** `statusCode: 200` or `201`. Frontend shows a generic “If an account exists, we’ve sent reset instructions” message. Backend sends a reset link to the email (link expires in 1 hour).

---

## 5. Google Sign-In (redirect)

**See [Google Sign-In – Step-by-Step Setup](GOOGLE_SIGNIN_SETUP.md) for full setup (Google Console, backend config, frontend, troubleshooting).**

**Endpoint:** `GET /auth/google`

**Query parameters:**

| Parameter     | Type   | Required | Description                                                                 |
|---------------|--------|----------|-----------------------------------------------------------------------------|
| redirect_uri  | string | Yes      | URL-encoded; where to send user after Google auth (e.g. `https://yourapp.com/dashboard/overview`) |

**Example:**  
`GET /auth/google?redirect_uri=https%3A%2F%2Fyourapp.com%2Fdashboard%2Foverview`

**Behaviour (backend):**
1. Redirect browser to Google OAuth consent.
2. User signs in with Google; Google redirects to backend callback.
3. Backend exchanges code for tokens, finds or **creates** user (if new), issues JWT.
4. Backend redirects user to `redirect_uri` with tokens in the URL fragment: `#access_token=...&refresh_token=...&expires_in=...&token_type=Bearer`

**New user (first-time Google sign-in):** Backend **creates** the user with **Super Admin** role. **Do not create any demo business or demo book**—only the user record. Tokens are issued and redirect contains tokens. Frontend should open the dashboard; if the user has **no businesses**, show an empty state and a clear **"Create new business"** (or "Create your first business") flow so they can create one.

Frontend must read the fragment after redirect and store tokens. If Google is not configured (`app.google.client-id` etc. empty), backend returns 503.

No request body. This is a browser redirect flow, not a JSON API call.

---

## 6. Current user (for role-based menu after Google / token login)

**Endpoint:** `GET /auth/me`

**Headers:** `Authorization: Bearer <access_token>`

**Payload:** None.

Used by the frontend after Google (or any) login when the user from the fragment or JWT has no or partial permissions. The frontend calls this to load the full user with `permissions` and `permissionScopes` so the role-based sidebar and screens show correctly.

**Success response:** Same wrapper, with `data` containing the current user (same shape as Verify OTP `data.user`):

```json
{
  "data": {
    "id": "string",
    "name": "string",
    "email": "string",
    "roleName": "string",
    "permissions": { "business": ["view", "create"], "book": ["view"], ... },
    "permissionScopes": { "business": { "view": "all" }, ... }
  },
  "message": "string",
  "statusCode": 200
}
```

If the token is missing or invalid, backend returns 401. If the user is not found or inactive, 401.

---

## Summary

| Action        | Method | Endpoint             | Payload / Notes                    |
|---------------|--------|----------------------|------------------------------------|
| Send OTP      | POST   | `/auth/send-otp`     | email, channel, clientId           |
| Verify OTP    | POST   | `/auth/verify-otp`   | email, otp, channel, clientId, intent? (login\|register) |
| Refresh token | POST   | `/auth/refresh`      | refreshToken                       |
| Forgot password | POST | `/auth/forgot-password` | email                          |
| Google Sign-In | GET   | `/auth/google`       | query: redirect_uri (URL-encoded)  |
| Current user  | GET    | `/auth/me`           | Bearer token; returns user with permissions |
