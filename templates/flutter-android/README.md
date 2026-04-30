# Copy these into your Flutter app

Your Cash Link screen references:

- `android/get-debug-sha1.ps1`
- `android/generate-google-services.ps1`

**Do this:**

1. Copy the **`android`** folder from here into your **Flutter project root** (merge with your existing `android` folder).
2. Run **`get-debug-sha1.ps1`** → add SHA-1 in **Google Cloud** (Android OAuth client, package `com.myledger.my_ledger_app`).
3. Run **`generate-google-services.ps1`** after you have a **`google-services.json`** from Firebase (it updates OAuth client entries; it does not replace the whole Firebase file).

See also: **`docs/GOOGLE_SIGNIN_COMPLETE_FIX.md`** in the backend repo.
