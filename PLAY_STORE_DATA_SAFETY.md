# Google Play Console Data Safety Form Guide

Use this reference when completing the **Data safety** questionnaire in Google Play Console for **JomaKhoroch (জমাখরচ)**.

---

## 1. Data Collection and Security Overview

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (HTTPS/TLS enforced by Firebase & Google APIs) |
| Do you provide a way for users to request that their data be deleted? | **Yes** (In-app Account & Data Deletion button + Email request) |

---

## 2. Data Types Declared

### A. Personal Info
- **Name:** Collected for account personalization (display name from Google Sign-In). Not shared. Optional for guests.
- **Email address:** Collected for authentication and account ownership. Not shared.

### B. Financial Info
- **Other financial info:** Income, Expense, Cash balance, Shop credit, Personal loan tracking.
  - *Purpose:* App functionality (Core financial ledger).
  - *Collection status:* Collected and synced to user's private Firestore account if logged in.
  - *Sharing:* **Not shared** with any third parties.

### C. Device or Other IDs
- **Device or other IDs:** Randomly generated sync device ID for resolving conflict timestamps.
  - *Purpose:* App functionality (cross-device sync cursor).
  - *Sharing:* **Not shared**.

---

## 3. Account Deletion URL
For the **"Provide a link that users can use to request that their account and associated data be deleted"** field in Play Console:
- **URL:** `https://github.com/emranmofficials/JomaKhoroch/blob/main/PRIVACY_POLICY.md#5-user-rights--data-deletion`
- Or email: `mailto:emranmofficials@gmail.com?subject=JomaKhoroch%20Account%20Deletion%20Request`
