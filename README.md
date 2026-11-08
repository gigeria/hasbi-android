# Hasbi (حسبي) — Android

> **Mera Hisab, Mera Saathi**
>
> Offline-first personal finance app for Pakistan. Daily expense tracking, Aaj Ka Baqi budget guidance, udhaar tracking, encrypted local backup.

[![Status](https://img.shields.io/badge/status-Week%201%20Bootstrap-success)]()
[![Phase](https://img.shields.io/badge/phase-1%20MVP-blue)]()
[![Min SDK](https://img.shields.io/badge/min%20SDK-26%20(Android%208.0)-green)]()

---

## 📱 About Hasbi

Hasbi is a native Android personal finance app built with Kotlin + Jetpack Compose. Phase 1 (MVP) focuses on:

- **Aaj Ka Baqi** — Daily Safe Spend hero feature (kitna paisa aaj kharch kar sakte hain)
- Multi-wallet tracking (Cash, Bank, JazzCash, Easypaisa, SadaPay, Credit Card, custom)
- Smart Entry (Roman Urdu + English natural language parsing, local rules only)
- Budget management with flexible cycles (monthly, salary-date, weekly, custom)
- Udhaar (debt) tracking without double-counting
- Encrypted local backup (AES-256 + Android Keystore + recovery passphrase)
- 30-day soft delete with auto-purge
- Privacy-first: no account, no cloud, no analytics on financial data

**Future vision:** Tax-ready records (Phase 2-3) + SBP Open Banking API integration (Phase 4).

---

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| Preferences | DataStore |
| DI | Hilt |
| Reactive | Kotlin Flow + Coroutines |
| Background | WorkManager |
| Splash | androidx.core:core-splashscreen |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## 📁 Project Structure

```
hasbi-android/
├── app/                                # Main application
├── core/
│   ├── core-database/                  # Room entities, DAOs, migrations
│   ├── core-datastore/                 # AppSettings, preferences
│   ├── core-design/                    # Material 3 theme, components, icons
│   ├── core-data/                      # Repository implementations
│   ├── core-domain/                    # Use cases, domain models (pure Kotlin)
│   └── core-util/                      # Date, money, crypto utilities
├── feature-onboarding/
├── feature-dashboard/
├── feature-transactions/
├── feature-wallets/
├── feature-budgets/
├── feature-debts/
├── feature-settings/
└── feature-backup/
```

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 35 (build tools 35.0.0)

### Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/hasbi-android.git
   cd hasbi-android
   ```

2. **Open in Android Studio**
   - File → Open → select `hasbi-android/` folder
   - Wait for Gradle sync to complete

3. **Run the app**
   - Select an emulator (Pixel 7, API 34+) or connect physical device
   - Click ▶️ Run button

4. **Verify Week 1 deliverable**
   - App launches with native splash screen (Hasbi logo on emerald background)
   - Dashboard placeholder shows: arch logo, "حسبي / Hasbi / Mera Hisab Mera Saathi"
   - Bottom navigation works (5 tabs switch content)
   - Other tabs show placeholder text indicating which week they'll be implemented

---

## 📋 Development Status

| Week | Module | Status |
|------|--------|--------|
| 1 | Project bootstrap (Gradle + theme + splash) | ✅ In Progress |
| 2 | core-database (entities, DAOs, seeders) | ⏳ Pending |
| 3 | core-data + core-domain (repos, use cases) | ⏳ Pending |
| 4 | feature-onboarding (3 screens, atomic init) | ⏳ Pending |
| 5 | feature-dashboard (Aaj Ka Baqi hero) | ⏳ Pending |
| 6 | feature-transactions (list, search, filter) | ⏳ Pending |
| 7 | feature-transactions (quick add, smart entry) | ⏳ Pending |
| 8 | feature-wallets (CRUD, transfer) | ⏳ Pending |
| 9 | feature-budgets (cycles, circular ring) | ⏳ Pending |
| 10 | feature-debts (lent/borrowed, no double-count) | ⏳ Pending |
| 11 | feature-backup + feature-settings | ⏳ Pending |
| 12 | Polish + Play Store launch | ⏳ Pending |

---

## 🎨 Brand Identity

| Element | Value |
|---------|-------|
| Name | Hasbi (حسبي) |
| Tagline | Mera Hisab, Mera Saathi |
| Primary color | Emerald `#1B5E43` |
| Accent | Warm Amber `#F4A340` |
| Premium accent | Gold `#C9A961` |
| Income | Soft Green `#4CAF80` |
| Expense | Warm Red `#E57373` (not aggressive) |
| Background | Warm Off-White `#F7F5EE` |
| Typography | Inter (system SansSerif in Week 1, bundled Inter in Week 2) |
| Cultural elements | Mosque silhouette, arch logo, "Assalam-o-Alaikum" greeting |

---

## 📜 Documentation

Full specifications are in the project root's `docs/` folder (or see `download/` from the AI agent):

- **Master Prompt v2** (`hasbi-master-prompt-v2.md`) — Complete 22-section product spec
- **Stage 2 Architecture** (`hasbi-stage2-technical-architecture.md`) — ER diagram, DAOs, repositories, use cases
- **Stage 3 Implementation Plan** (`hasbi-stage3-implementation-plan.md`) — 12-week sprint plan
- **Visual Preview** (`hasbi-preview.html`) — 21 HTML mockups

---

## 🔒 Privacy & Security

- **No account required** — install and use
- **No internet needed** — fully offline-first
- **No analytics** — financial data never leaves device
- **No permissions** — onboarding doesn't request Contacts/SMS/Location/Storage
- **Encrypted backup** — AES-256-GCM + Android Keystore + recovery passphrase
- **Local-first** — Room DB on device, optional Google Drive backup in Phase 2

---

## 🗺 Roadmap

- **Phase 1 (Weeks 1-12):** MVP — manual entry, Aaj Ka Baqi, budgets, debts, local backup
- **Phase 1.1 (3 months post-launch):** Savings goals, recurring transactions, tags, biometric PIN
- **Phase 2.1 (6 months):** Personal vs Business transaction classification
- **Phase 2.2 (9 months):** CSV/Excel bank statement import
- **Phase 3 (12 months):** Annual tax summary, FBR-format exports
- **Phase 4 (18-24 months):** SBP Open Banking API integration (when regulatory framework matures)

---

## 📄 License

Proprietary. All rights reserved.

---

## 💚 Acknowledgments

Hasbi (حسبي) takes its name from the Arabic root H-S-B, related to Al-Hasib (الحسيب) — one of the names of Allah meaning "The Reckoner." Hasbi means "sufficient for me" — a personal financial companion.

Built with care for the Pakistani audience, with Roman Urdu-friendly copy and PKR-first defaults.
