# Google Sign-In on Android (Cash Link / My Ledger)
#testing
**Full step-by-step checklist:** see **[GOOGLE_SIGNIN_COMPLETE_FIX.md](./GOOGLE_SIGNIN_COMPLETE_FIX.md)** (Google Cloud, Firebase, Flutter, backend).

**SHA-1 script (run against your Flutter project folder):** `scripts/get-debug-sha1.ps1` in this repo.

The error **â€œadd your debug SHA-1 in Google Cloudâ€** happens **before** your app can get a valid Google ID token. The backend cannot remove that; you must configure Google Cloud + the app as below.

## 1. Register SHA-1 in Google Cloud Console

1. Open [Google Cloud Console](https://console.cloud.google.com/) â†’ the **same project** as your **Web client ID** (`app.google.client-id` in the backend).
2. **APIs & Services** â†’ **Credentials**.
3. **Create credentials** â†’ **OAuth client ID** â†’ type **Android**.
4. **Package name:** `com.cashlink.my_ledger_app` (must match `applicationId` in `android/app/build.gradle`).
5. **SHA-1 certificate fingerprint:** your **debug** keystore (for development):

   **Windows (PowerShell), from your Flutter/Android project root:**
   ```powershell
   cd android
   .\gradlew.bat signingReport
   ```
   Copy the **SHA1** under `Variant: debug`.

   Or use your projectâ€™s script if you have one:
   ```powershell
   powershell -File android/get-debug-sha1.ps1
   ```

6. Save the Android OAuth client.

## 2. Use the **Web** client ID in the app (important)

For Flutter `google_sign_in` / Firebase, you usually pass the **Web application** OAuth client ID (the same as `757686541997-....apps.googleusercontent.com` from the backend) into **`requestIdToken(...)`** (or the equivalent in your plugin).  
That way the **ID tokenâ€™s `aud` (audience)** matches what the backend verifies.

Do **not** use the Android client ID string as `requestIdToken` unless you also add that client ID to the backend:

```properties
# application.properties â€” optional, comma-separated
app.google.additional-audiences=<your-android-oauth-client-id>.apps.googleusercontent.com
```

## 3. Call the backend after sign-in

After Google Sign-In succeeds, send the **ID token** (JWT), not the access token:

**`POST`** `{baseUrl}/auth/google/id-token`  
**Body:**
```json
{
  "idToken": "<paste idToken from GoogleSignInAccount.getIdToken() or Flutter equivalent>"
}
```

**Success:** same shape as OTP login â€” `data.accessToken`, `data.refreshToken`, `data.user`, etc.

## 4. Firebase (if you use it)

If the app uses Firebase Auth with Google:

- In Firebase Console â†’ Project settings â†’ your Android app â†’ add the **same debug SHA-1**.
- Download/update `google-services.json`.

## 5. Release builds

For Play Store / release APK, create another **Android** OAuth client (or use the upload key SHA-1) and add that SHA-1 in Google Cloud as well.

## Checklist

| Step | Action |
|------|--------|
| â˜ | SHA-1 from `gradlew signingReport` (debug) added to **Android** OAuth client |
| â˜ | Package name = `com.cashlink.my_ledger_app` |
| â˜ | `requestIdToken` uses **Web** client ID (same as backend `app.google.client-id`) |
| â˜ | App calls `POST /cashlink-api/auth/google/id-token` with `idToken` |

After SHA-1 is correct, native Google Sign-In should return a real `idToken`; the backend will verify it and issue your JWTs.
