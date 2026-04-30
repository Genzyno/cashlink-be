# Complete fix: Google Sign-In on Android (Cash Link)

The red error means Google **rejects** Sign-In until your **debug keystore SHA-1** is registered. The backend cannot remove this requirement; follow **every** step below.

---

## Part 0 — Scripts your app UI mentions (`android/…`)

Copy these from the **backend** repo into your **Flutter** project’s `android` folder:

| Copy from (backend) | To (Flutter) |
|---------------------|----------------|
| `templates/flutter-android/android/get-debug-sha1.ps1` | `android/get-debug-sha1.ps1` |
| `templates/flutter-android/android/generate-google-services.ps1` | `android/generate-google-services.ps1` |

---

## Part A — Get your debug SHA-1

### Option 1: `android/get-debug-sha1.ps1` (after Part 0)

```powershell
cd "D:\path\to\your\flutter\app"
powershell -File android\get-debug-sha1.ps1
```

### Option 1b: Backend `scripts/get-debug-sha1.ps1` (any Flutter path)

```powershell
cd "D:\path\to\your\flutter\app"
powershell -File "D:\JOHN\My_Projects\my-ledger-be\scripts\get-debug-sha1.ps1" -FlutterRoot "D:\path\to\your\flutter\app"
```

### Option 2: Gradle manually

```powershell
cd "D:\path\to\your\flutter\app\android"
.\gradlew.bat signingReport
```

In the output, under **`Variant: debug`**, copy **`SHA1:`** (format `AA:BB:CC:...`).

---

## Part B — Google Cloud Console (required)

1. Open [Google Cloud Console](https://console.cloud.google.com/) → select the **same project** as your **Web client ID** (the one in `application.properties` → `app.google.client-id`).
2. **APIs & Services** → **Credentials**.
3. **+ Create credentials** → **OAuth client ID**.
4. Application type: **Android**.
5. **Name:** e.g. `Cash Link Android Debug`.
6. **Package name:** `com.myledger.my_ledger_app` (must match `applicationId` in `android/app/build.gradle`).
7. **SHA-1 certificate fingerprint:** paste the **debug SHA-1** from Part A.
8. **Create**.

If **OAuth client ID** is disabled, configure **OAuth consent screen** first (User type, app name, save).

---

## Part C — Firebase (only if the app uses Firebase)

1. [Firebase Console](https://console.firebase.google.com/) → your project → **Project settings** (gear).
2. Under **Your apps** → Android app with package `com.myledger.my_ledger_app`.
3. **Add fingerprint** → paste the **same debug SHA-1**.
4. Download **`google-services.json`** and save to `android/app/google-services.json`.
5. **Patch OAuth clients in JSON** (adds Android client + cert hash Firebase expects):

   ```powershell
   cd "D:\path\to\your\flutter\app"
   powershell -File android\generate-google-services.ps1 `
     -AndroidClientId "YOUR_ANDROID_CLIENT_ID.apps.googleusercontent.com" `
     -Sha1 "AA:BB:CC:DD:..." `
     -WebClientId "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
   ```

   Use the **Android** OAuth Client ID from Google Cloud (Credentials), not the Web one, for `-AndroidClientId`.  
   `-WebClientId` should match backend `app.google.client-id` (used by `serverClientId` / ID token).

6. Rebuild: `flutter clean` then `flutter run`.

---

## Part D — Flutter / Android app code

1. **Web Client ID** in Sign-In (must match backend `app.google.client-id`):

   ```dart
   // Example — use YOUR Web client ID string ending in .apps.googleusercontent.com
   final googleSignIn = GoogleSignIn(
     scopes: ['email', 'profile'],
     serverClientId: '757686541997-2uupjjklvi42lkq5r9u5uis2gd2cdemm.apps.googleusercontent.com',
   );
   ```

   Or the equivalent `GoogleSignIn` / `requestScopes` setup your project uses — the important part is **`serverClientId`** (or `clientId` for id token) = **Web** OAuth client ID.

2. After successful Google sign-in, send the **ID token** to your backend (not the browser OAuth flow):

   ```http
   POST https://<YOUR_SERVER>/myledger-api/auth/google/id-token
   Content-Type: application/json

   { "idToken": "<value from account.authentication.idToken>" }
   ```

3. Use `data.accessToken` / `data.refreshToken` from the response like OTP login.

---

## Part E — Release builds (Play Store / release APK)

Debug SHA-1 ≠ release SHA-1. For release:

1. Run `signingReport` and copy **release** variant SHA-1, **or** use your upload keystore.
2. Add another **Android** OAuth client (or add fingerprint) in Google Cloud with that SHA-1.
3. Add the same fingerprint in Firebase if applicable.

---

## Checklist (do all)

| # | Done | Step |
|---|------|------|
| 1 | ☐ | Ran `signingReport` and copied **debug** SHA-1 |
| 2 | ☐ | Created **Android** OAuth client in Google Cloud: package `com.myledger.my_ledger_app` + SHA-1 |
| 3 | ☐ | OAuth consent screen configured in same project |
| 4 | ☐ | Firebase: same SHA-1 added + `google-services.json` updated (if using Firebase) |
| 5 | ☐ | App uses **Web** client ID for `serverClientId` / `requestIdToken` |
| 6 | ☐ | App calls `POST .../auth/google/id-token` with `idToken` after sign-in |

After **1–2**, the on-device Google Sign-In dialog should succeed. After **6**, your backend issues JWTs.

---

## Still failing?

- **Error 10 / DEVELOPER_ERROR:** Almost always wrong package name or missing/wrong SHA-1 in Google Cloud.
- **Different machine:** Each PC’s debug keystore can differ — add **that machine’s** debug SHA-1.
- **Wrong Google project:** Android client must be in the **same** project as the Web client ID used in the app and backend.

See also: `GOOGLE_ANDROID_SIGNIN.md` and `ANDROID_AUTH_API.md` section **2b**.
