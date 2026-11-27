# Privacy Policy for JomaKhoroch (জমাখরচ)

**Last updated:** November 27, 2026

**JomaKhoroch (জমাখরচ)** ("we", "our", or "the app") is committed to protecting your privacy and personal information. This Privacy Policy explains how our personal and small-business financial management application collects, uses, and safeguards your information when you use our mobile application.

---

## 1. Information We Collect

### A. Personal Information
- **Google Account Information:** When you sign in using Google Sign-In, we receive your basic profile information (such as your display name, email address, and unique Google/Firebase User ID).
- **Guest / Anonymous Users:** If you use the app as a guest, a temporary anonymous ID is generated to facilitate local persistence without requiring email or name.

### B. Financial & Ledger Data
To provide the app's core accounting, bookkeeping, and budgeting features, you enter financial information including:
- Cash in hand and bank/mobile wallet balances
- Income and expense transactions, dates, amounts, and categories
- Neighborhood shop credit (দোকান বাকি) records and repayments
- Personal lending and borrowing (ধার ও দেনা) details
- Monthly budget thresholds and targets
- Personal diary notes, reminders, and checklists

### C. Device and Technical Information
- **Device Identifiers:** A randomly generated local device identifier is stored alongside your sync cursor to reconcile offline changes across multiple devices.
- **Log and Diagnostic Data:** Technical logs required to facilitate network retry and cloud synchronization.

---

## 2. How We Use Your Information

We use the collected information solely for providing and improving the app:
1. **Core Accounting:** Accurately calculating account balances, due payments, loan rings, and financial reports.
2. **Offline-First Synchronization:** Synchronizing your ledger securely with Cloud Firestore when your device is connected to the internet.
3. **Local Notifications:** Delivering scheduled reminders for loan due dates, budget warnings (80% and 100%), and daily accounting check-ins.
4. **App Updates:** Checking for official application updates via GitHub Releases.

We **do not** sell, rent, monetize, or share your financial data, notes, or personal information with any third-party advertisers or data brokers.

---

## 3. Data Storage & Security

- **Local Storage:** Your entries are stored on your device in a secure SQLite/Room database.
- **Encrypted Backups:** If you generate an export (`.jkbackup`), the file is protected with military-grade AES-256-GCM encryption and salted PBKDF2 key derivation.
- **PIN & Biometric Lock:** An optional 4-digit PIN lock and biometric authentication protect unauthorized access on your device.
- **Cloud Security:** Cloud synchronization is hosted on Google Cloud / Firebase Firestore and enforced by strict security rules that isolate each user's records to their authenticated User ID.

---

## 4. Third-Party Services

The app utilizes trusted infrastructure services provided by Google:
- **Firebase Authentication:** For secure user sign-in and account verification.
- **Cloud Firestore:** For cross-device encrypted cloud backup.
- **Google Play Services:** For Credential Manager authentication and notification delivery.

Each third-party provider processes data in accordance with their respective privacy policies:
- [Google Privacy Policy](https://policies.google.com/privacy)
- [Firebase Privacy Information](https://firebase.google.com/support/privacy)

---

## 5. User Rights & Data Deletion

### A. In-App Data & Account Deletion
You maintain full ownership of your data at all times:
- You can delete individual books, transactions, notes, or contacts within the app.
- You can permanently delete your cloud account and all associated Firestore records directly from **More / Settings → Delete Account & Cloud Data**.

### B. Web Deletion Request
You may also request complete removal of your account and stored records by emailing us with your registered email address.

---

## 6. Children's Privacy

JomaKhoroch is intended for personal and business bookkeeping by individuals aged 18 and above. We do not knowingly collect personal information from children under 13.

---

## 7. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. Any changes will be posted in the application repository with an updated "Last updated" date.

---

## 8. Contact Us

If you have any questions or concerns regarding this Privacy Policy or your data, please contact:

- **Developer:** Emran Mahmud
- **Email:** emranmofficials@gmail.com
- **Project Repository:** [https://github.com/emranmofficials/JomaKhoroch](https://github.com/emranmofficials/JomaKhoroch)
