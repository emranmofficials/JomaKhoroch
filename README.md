# JomaKhoroch (জমাখরচ)

**প্রতিদিনের হিসাব, হাতের মুঠোয়।** — A 100% offline, privacy-first personal finance manager, shop-due ledger, personal lending & borrowing tracker, monthly budget planner, report exporter, and digital pocket diary built natively for Android in Kotlin and Jetpack Compose (Material 3).

---

## Release Highlights (`v1.0.0`)

- **100% Offline & Private:** Zero `INTERNET` permission, zero analytics, zero trackers, and zero cloud dependency. All records remain strictly on the user's device.
- **Splash & Interactive Onboarding:** Branded emerald-and-gold splash launch, custom editorial illustrations, instant Bangla/English selection, and optional opening cash balance setup on first run.
- **Integer-Poisha Accounting Engine:** All balances and calculations execute strictly in integer poisha (`Long`) with zero floating-point rounding drift and airtight double-count prevention:
  - Shop credit expenses are recognized once at purchase; subsequent due payments reduce cash and shop payable without inflating expenses.
  - Personal lending and borrowing track cashflow and receivables/payables independently from income, expenses, and shop dues.
- **Multiple Independent Account Books:** Preloaded with Personal (`ব্যক্তিগত`), Family (`পারিবারিক`), and Business (`ব্যবসা`) books plus custom notebooks with strict SQLite isolation and safe archive/delete confirmation.
- **Shop Due & Personal Loan Ledgers:** Supports Quick Amount Entry and Itemized Entry (item, quantity, unit, unit price), partial or full settlements, full shop statements, and edit/delete audit logs.
- **Digital Pocket Diary & Offline Reminders:** Searchable color-coded notes, interactive shopping checklists, contact cards, pinned notes, zero-permission Android Photo Picker local attachments, and local notification reminders.
- **Budgets, Analytics & Offline Export:** Monthly overall & category budgets with 80%/100% warnings; Daily/Weekly/Monthly/Yearly reports with offline PDF & UTF-8 CSV export via Storage Access Framework (SAF).
- **Security & Encrypted Backup:** Optional 4-digit PIN lock (salted PBKDF2-HMAC-SHA256), biometric unlock, and passphrase-encrypted AES-256-GCM `.jkbackup` export/restore with SHA-256 integrity verification.
- **Bangla & English Localization:** Full Bangla-first UI with Bangla/English numerals toggle (`০১২৩` / `0123`) and System/Light/Dark Material 3 themes.

---

## Tech Stack & Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3 + Custom Canvas Charts + Adaptive Navigation (`NavigationBar` / `NavigationRail`)
- **Architecture:** MVVM (`ViewModel` + `StateFlow`) + Repository Pattern + Clean Pure-Kotlin `AccountingEngine`
- **Local Persistence:** Room SQLite + Jetpack Preferences DataStore
- **Security:** `PBKDF2WithHmacSHA256` PIN hashing + `AES/GCM/NoPadding` encrypted `.jkbackup` archives
- **Google Play Policy Compliant:** Uses zero-permission Android Photo Picker (`PickVisualMedia`) and Storage Access Framework (`CreateDocument` / `OpenDocument`); no broad storage permissions requested.

---

## Building & Testing Locally

```bash
# Run unit tests (Accounting Engine & PIN Security)
gradle :app:testDebugUnitTest --tests "com.example.AccountingEngineTest" --tests "com.example.PinSecurityTest"

# Assemble Debug APK
gradle :app:assembleDebug

# Assemble Release APK / AAB (requires signing environment variables)
gradle :app:assembleRelease :app:bundleRelease
```

---

## Official GitHub Release Automation

This repository includes `.github/workflows/release.yml` for automated CI verification and official GitHub Releases:

1. Push this repository to GitHub (via AI Studio **Push to GitHub** or `git push` to `main`/`master`).
   - GitHub Actions automatically runs unit tests, builds `JomaKhoroch-v1.0.0-release.apk`, `.aab`, and `-debug.apk`, uploads them as workflow artifacts, and creates/updates the **GitHub Release** for `v1.0.0` (matching `versionName` in `app/build.gradle.kts`).
2. *(Optional for Google Play upload signing)* Add the following Repository Secrets in **GitHub -> Settings -> Secrets and variables -> Actions**:
   - `SIGNING_KEYSTORE_BASE64` — Base64-encoded upload keystore (`.jks`) with alias `upload`
   - `KEYSTORE_PASSWORD` — Keystore password
   - `KEY_PASSWORD` — Key password
   *(If not configured, the workflow signs the release APK using the repository's deterministic fallback keystore so GitHub Releases still publish an installable APK automatically.)*
3. You can also cut a new version release anytime by pushing a tag (`git tag v1.0.1 && git push origin v1.0.1`) or running **Actions -> Android CI & Official GitHub Release -> Run workflow**.

---

## License

Licensed under the [MIT License](LICENSE).
