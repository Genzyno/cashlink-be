# Google Sign-In â€“ Step-by-Step Setup

This guide gets Google Sign-In working from the frontend (localhost:4200) through the backend (localhost:8080) and Google OAuth.

---

## Overview

1. **User** clicks "Sign in with Google" on the login page.
2. **Browser** goes to: `http://localhost:4200/cashlink-api/auth/google?redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fdashboard%2Foverview`
3. **Angular proxy** forwards that to: `http://localhost:8080/cashlink-api/auth/google?redirect_uri=...`
4. **Backend** redirects the browser to **Google** sign-in.
5. **User** signs in with Google; Google redirects back to your **backend callback** (e.g. `http://localhost:8080/cashlink-api/auth/google/callback`).
6. **Backend** exchanges the code for tokens, creates/finds the user, then redirects the browser to:  
   `http://localhost:4200/dashboard/overview#access_token=...&refresh_token=...&expires_in=...&token_type=Bearer`
7. **Frontend** (startup) reads the hash, stores tokens and user, and clears the hash.

---

## Step 1: Google Cloud Console (OAuth client)

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project.
3. Open **APIs & Services** â†’ **Credentials**.
4. Click **Create Credentials** â†’ **OAuth client ID**.
5. If asked, configure the **OAuth consent screen** (User type: External, add your app name and support email).
6. Application type: **Web application**.
7. Name: e.g. `My Ledger Web`.
8. **Authorized redirect URIs** â€“ add **exactly** the URL your backend uses for the Google callback, for example:
   - Local: `http://localhost:8080/cashlink-api/auth/google/callback`  
   - Or whatever path your backend actually uses (e.g. `http://localhost:8080/cashlink-api/auth/google/oauth2/callback`).
9. Save. Copy the **Client ID** and **Client Secret** â€“ the backend will need these.

---

## Step 2: Backend (e.g. Spring Boot)

Your backend must:

1. **Serve**  
   `GET /cashlink-api/auth/google?redirect_uri=<url>`  
   (or `GET /auth/google` if your app is mounted under `/cashlink-api`).

2. **On GET /auth/google:**
   - Read `redirect_uri` from the query (this is the **frontend** URL, e.g. `http://localhost:4200/dashboard/overview`).
   - Store it in the session (or in the state parameter) for the callback.
   - Redirect the browser to Google's authorization URL with:
     - `client_id` = your Google Client ID
     - `redirect_uri` = **your backend callback URL** (e.g. `http://localhost:8080/cashlink-api/auth/google/callback`) â€“ must match Google Console exactly
     - `response_type=code`
     - `scope=openid email profile`
     - `state` = optional but recommended (e.g. encode the frontend `redirect_uri` in state so you know where to send the user back)

3. **Google** will redirect to your **backend** callback with `?code=...&state=...`.

4. **Backend callback** (e.g. `GET /auth/google/callback`):
   - Exchange `code` for tokens with Google (POST to `https://oauth2.googleapis.com/token`).
   - Get user info from Google (e.g. `https://www.googleapis.com/oauth2/v2/userinfo`).
   - Find or create the user in your DB and generate **your** JWT access + refresh tokens.
   - Redirect the browser to the **frontend** URL you stored (from `redirect_uri`), with tokens in the **fragment** (hash), for example:
     - `http://localhost:4200/dashboard/overview#access_token=<jwt>&refresh_token=<refresh>&expires_in=3600&token_type=Bearer`
   - Optionally append `&user=<url-encoded-json>` if you want to send the user object in the fragment.

5. **Configuration** (e.g. `application.properties`):
   - `app.google.client-id` = Google Client ID
   - `app.google.client-secret` = Google Client Secret
   - `app.google.redirect-uri` = backend callback URL (must match Google Console exactly), e.g. `http://localhost:8080/cashlink-api/auth/google/callback`
   - If these are missing, your backend returns **503** as per Auth API doc.

---

## Step 3: Frontend (already done)

- **Login page:** "Sign in with Google" button calls `auth.googleSignIn()`.
- **Redirect URL:**  
  `{apiUrl}auth/google?redirect_uri={encodeURIComponent(origin + '/dashboard/overview')}`  
  So with `apiUrl = '/cashlink-api/'` and origin `http://localhost:4200` you get:
  - `http://localhost:4200/cashlink-api/auth/google?redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fdashboard%2Foverview`
- **Proxy:** `proxy.conf.js` forwards `/cashlink-api` to `http://localhost:8080`, so the request hits the backend.
- **After redirect:** Startup runs `processFragmentFromGoogleRedirect()`, which reads `access_token`, `refresh_token`, etc. from the hash, stores them, and clears the hash.

No frontend code change is needed if the backend returns the fragment as above.

---

## Step 4: Run and test

1. **Start backend** on port 8080 (or the port in `proxy.conf.js`).
2. **Start frontend:**  
   `ng serve`  
   (so proxy is active).
3. Open: `http://localhost:4200/passport/login`.
4. Click **Sign in with Google**.
5. You should be sent to Google, then back to your backend, then to `http://localhost:4200/dashboard/overview#access_token=...`; the app should then show the dashboard and clear the hash.

---

## Checklist

| Step | Check |
|------|--------|
| 1 | Google Cloud: OAuth 2.0 Web client created, **Authorized redirect URI** = backend callback URL (e.g. `http://localhost:8080/cashlink-api/auth/google/callback`) |
| 2 | Backend: `GET /auth/google` implemented, reads `redirect_uri`, redirects to Google with correct `client_id` and backend `redirect_uri` |
| 3 | Backend: Callback endpoint exchanges `code`, creates/finds user, redirects to frontend **with tokens in fragment** |
| 4 | Backend: Runs on port 8080 (or update `proxy.conf.js` target) |
| 5 | Frontend: `ng serve` so proxy is used; you use `http://localhost:4200` (not 127.0.0.1) so `redirect_uri` is consistent |

---

## If it "doesn't work"

- **404 on `/cashlink-api/auth/google`**  
  Backend not running, or path not mounted under `/cashlink-api`, or proxy target/port wrong in `proxy.conf.js`.

- **Google error "redirect_uri_mismatch"**  
  The redirect URI in Google Console must **exactly** match the backend callback URL (including path and `http` vs `https`). No trailing slash unless the backend uses it.

- **Back to frontend but not logged in**  
  Backend must redirect with **fragment** (`#access_token=...`), not query (`?access_token=...`). Frontend only reads the hash.

- **503 from backend**  
  Backend returns 503 when Google is not configured (e.g. missing `app.google.client-id`). Add Client ID and Secret and callback URL in backend config and in Google Console.
