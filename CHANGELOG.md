# Changelog

All notable changes to **JomaKhoroch (জমাখরচ)** are documented in this file.

## [1.5.2] - Startup Crash Fix & Offline Resilience

### Fixed
- **Startup Crash Without `google-services.json`:** Made `CloudSyncRepository` lazily resolve `FirebaseFirestore` and `FirebaseAuth` only when `FirebaseApp` is initialized, preventing `IllegalStateException: Default FirebaseApp is not initialized` crashes on app launch.
- **Seamless Offline Fallback:** Added safe guards in `JomaKhorochViewModel`, `GoogleAuthManager`, `FirestoreErrorHandler`, and `MainActivity` so the app runs cleanly in offline mode when Firebase configuration is absent.

## [1.5.1] - Dashboard UI Polish & Privacy Architecture

### Improved
- **Unified Editorial Section Headers:** Standardized all dashboard sections (কুইক অ্যাকশন, দেনা-পাওনা ও বাকি, মাসিক সারসংক্ষেপ, আজকের লেনদেন, সাম্প্রতিক লেনদেন) with consistent `SectionHeader` components and direct "সব দেখুন →" navigation shortcuts.
- **2-Line Squircle Quick Tool Labels:** Quick action buttons now accommodate 2-line Bengali labels with uniform vertical baselines, preventing ellipses or truncated text on compact mobile screens.
- **Enhanced Privacy Mode:** Toggling balance visibility (`hideBalance`) now seamlessly shields all currency amounts across the entire transaction ledger (`৳ ••••`), providing full privacy in public spaces.
- **Symmetric Dues & Loans Overview:** Re-architected `SplitReceivablePayableCard` into a clean dual-column layout with 16dp generous padding, squircle touch feedback, and seamless elevation.
- **Streamlined Top Toolbar:** Aligned sync pill, notification bell badge, and privacy toggle with a uniform 38dp height, 12dp rounded squircle corners, and clean hairline outlines.


## [1.5.0] - Streamlined 4-Tab Navigation & Pure Financial Focus

### Changed
- **Pure 4-Tab Navigation:** Streamlined bottom navigation bar from 5 tabs down to 4 focused financial tabs:
  1. **হোম (Home):** Cash balance, quick income/expense actions, financial tools, and today's transactions.
  2. **লেনদেন (Transactions):** Complete ledger history with date filtering, categories, and payment methods.
  3. **হিসাব (Accounts):** Shop dues (দোকানের বাকি), personal loans given & borrowed (ধার-দেনা), and multi-book management.
  4. **আরও (More):** Monthly budgets, analytics reports, cloud backup & sync, and application settings.
- **Removed Diary & Task Bloat:** Eliminated non-financial pocket diary and checklist tabs to reduce cognitive load, decrease navigation clutter, and deliver an intuitive, distraction-free accounting workflow.
- **100% Financial Quick Tools:** Replaced the legacy diary quick action on the dashboard with `QuickActionType.RECORD_PAYMENT` (বাকি পরিশোধ) with quick payment launcher.


## [1.4.2] - Accounts Screen Editorial Redesign

### Added
- **Editorial Accounts Masthead:** Harmonized the Accounts screen (দোকান, ধার দেওয়া, ধার নেওয়া, খাতা) with the app-wide Editorial Paper & Ink design system.
- **Unified 4-Segment Pill Dock:** Refined tab switcher with high-contrast active containers, smooth color transitions, and icon badges.
- **Tabular Figures & Metrics:** Formatted all shop dues, personal loans, repayment rings, and multi-book ledger totals with monospace tabular numbers ("tnum") for clean visual alignment.

## [1.4.1] - Professional UI & Layout Optimization

### Improved
- **Hero Financial Vault Card:** Elevated Cash Balance presentation with rounded container, tabular numerals, inline wallet badge, and soft pastel dual-metric capsules for income and expense.
- **Enhanced Primary Dock Buttons:** Bold 48dp tactile action buttons for `+ জমা` and `- খরচ` with inner icon containers for instant recognition.
- **Tactile Quick Actions Strip:** Upgraded quick tools (`দোকানের বাকি`, `হাতে ধার দিন`, `ধার নিন`, `নোট`) with squircle pastel badge containers and crisp typography.
- **Unified Loans & Dues Overview Card:** Rebuilt Split Receivable & Payable card with top section header, status pill tags, and prominent tabular figures.
- **Refined Transaction Rows:** Added squircle category icon containers, payment method pill chips, and bold tabular amounts across Dashboard and Transactions screens.

## [1.4.0] - Official Release: Editorial Paper & Ink Redesign

### Added
- **Editorial Paper & Ink Design System:** Reimagined visual architecture featuring an archival ledger aesthetic with warm paper backgrounds, slate and teal ink accents, crisp hairline dividers, and brass nib metallic details.
- **Editorial Paper Masthead:** Redesigned Dashboard and Transactions header with contextual metadata, live sync badge, book balance card, and seamless scroll integration.
- **Tabular Numerals ("tnum"):** Enhanced typography with monospace numeric alignment across all currency amounts, ledger entries, and statements for effortless visual scanning.

### Improved
- **Direct In-App APK Downloader:** Updates download directly inside the app with real-time percentage and byte counters, seamlessly invoking the native Package Installer.
- **Bulletproof Google Sign-In & Sync:** Retained all Google Services and Firebase OAuth resources against R8 shrinking with compile-time fallbacks.
- **Play Store Ready:** Fully compliant with Google Play Data Safety, Account Deletion, edge-to-edge system bar theming, and optimized 5.5MB release footprint.

## [1.3.1] - Google Sign-In Resource Shrinking Fix

### Fixed
- **Retained Google Services Resources in R8 Shrinking:** Added `res/raw/keep.xml` to prevent AGP resource shrinking from stripping `@string/default_web_client_id` and Firebase strings in release builds.
- **Fail-Safe Web Client ID Fallback:** Added compile-time `BuildConfig.DEFAULT_WEB_CLIENT_ID` fallback in `GoogleAuthManager` so Google Sign-In always resolves the OAuth Client ID and never encounters missing configuration errors.

## [1.3.0] - Direct In-App APK Download & One-Tap Installer

### Added
- **Direct In-App APK Downloader:** Updates now download directly inside the application dialog with real-time progress percentage bar, downloaded size indicator, and network redirect support — completely removing the need to open an external web browser.
- **Native Android Package Installer Prompt:** After in-app download finishes, the app immediately triggers the native Android system package installer via secure `FileProvider`.
- **Package Install Permission Support:** Integrated Android 8+ `REQUEST_INSTALL_PACKAGES` workflow for zero-hassle in-app upgrades.

## [1.2.0] - Production Ready & Play Store Compliance

### Added
- **Play Store Compliant Account & Cloud Data Deletion:** Added permanent "Delete Account & Cloud Data" option in Settings with confirmation dialog to comply with Google Play User Data policies.
- **Privacy Policy & Play Store Guides:** Added comprehensive `PRIVACY_POLICY.md`, `PLAY_STORE_DATA_SAFETY.md`, and `STORE_LISTING.md` metadata guides.

### Improved
- **R8 / ProGuard Code & Resource Shrinking:** Enabled R8 minification and resource shrinking in release builds, reducing download size and optimizing runtime speed.
- **Enhanced ProGuard Keep Rules:** Hardened keep rules for Credential Manager, Google Identity, and Room entities.

## [1.1.2] - Instant Auth Transition & Guest Mode

### Fixed
- Fixed Compose state transition so Google Sign-In immediately opens the dashboard without hanging.
- Added Guest / Instant entry mode backed by Firebase Anonymous Authentication for one-tap sign-in.
- Added all previous release SHA-1 signatures to Firebase to maintain backward compatibility.

## [1.1.1] - Google Sign-In & Keystore Fix

### Fixed
- Bundled complete `google-services.json` with dedicated Android OAuth client configuration.
- Configured dedicated permanent release signing keystore and registered SHA-1 and SHA-256 fingerprints in Firebase Console to fix Google Sign-In authentication.

## [1.1.0] - Cloud Sync, Notifications & App Updates

### Added
- **Online + Offline Cloud Sync:** Firebase Firestore cloud backup with Google Sign-In, real-time cross-device sync, offline-first Room persistence, and automatic pending-change sync when connectivity returns.
- **Complete Notification System:** Dedicated Android notification channels for Budget Alerts (80% & 100%), Loan Due-Date Reminders, Daily Accounting Check-ins, Task Reminders, and App Updates — with boot-time rescheduling and an in-app Notifications Center.
- **In-App Update Checker & Release Alerts:** Automatic and manual GitHub Release checks, push notifications when a new version is published, Dashboard update banner, and one-tap Play-Policy-compliant APK download via browser.

### Improved
- Live online/offline sync badge and alert bell indicator on the Home Dashboard header.
- Automatic GitHub repository binding during GitHub Actions release builds so installed APKs detect future updates with zero setup.

## [1.0.0] - Official Release

### Added
- **Splash Screen & Onboarding:** Seamless emerald cold-start window theme, animated brand splash screen, and a 3-slide editorial onboarding flow with custom illustrations, instant Bangla/English toggle, and optional opening cash balance setup.
- **Core Accounting Engine:** Integer-only poisha (`Long`) accounting with strict double-count prevention across cash, shop credit, and personal lending/borrowing.
- **Multi-Book Ledger:** Preloaded Personal, Family, and Business notebooks plus custom book creation, switching, archiving, and protected permanent deletion.
- **Shop Due & Loan Management:** Quick and itemized shop credit entry, partial/full settlements, shop statements, personal lending/borrowing cards with circular repayment rings, and audit history logs.
- **Pocket Diary & Reminders:** Searchable color-coded notes, interactive checklists, contact cards, local Photo Picker attachments, and offline local notifications.
- **Budgets, Reports & Exports:** Overall and category monthly budgets with progress alerts, Daily/Weekly/Monthly/Yearly reports, and offline PDF & CSV export via SAF.
- **Security & Encrypted Backup:** 4-digit salted PBKDF2 PIN lock, biometric unlock, and AES-256-GCM `.jkbackup` export/restore with SHA-256 integrity checks.
- **GitHub Release CI/CD:** Automated unit testing, APK/AAB builds, and GitHub Release publishing via `.github/workflows/release.yml`.

### Improved
- Unified Curved Editorial Canopy + Floating Dock design language across all 5 main tabs (Home, Accounts, Transactions, Diary, More).
- Removed redundant floating buttons and duplicate empty-state actions for a cleaner, clutter-free UI.
- Added production R8/ProGuard keep rules and Play Policy compliant zero-permission media/file handling.
