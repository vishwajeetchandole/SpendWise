<p align="center">
  <img src="art/app-icon-512.png" alt="SpendWise Logo" width="120"/>
</p>

<h1 align="center">SpendWise</h1>

<p align="center">
  A private, offline-first personal finance & expense manager for Android built with Kotlin and Jetpack Compose.<br/>
  Log daily expenses, track budgets, review monthly analytics, and export detailed financial reports. Your data never leaves your phone.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 7.0+"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-1.7.5-4285F4?logo=android&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Database-Room-2563EB" alt="Room"/>
  <img src="https://img.shields.io/badge/Works-100%25%20Offline-1D4ED8" alt="Offline"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"/>
</p>

---

## 🌟 Overview

SpendWise is built for seamless daily expense tracking and financial management. Add expenses with category tags, custom notes, dates, and payment details. View real-time visual charts and monthly spending summaries. With no external server requirements, no ads, and no tracking, all your profile data, PINs, and financial logs remain encrypted and saved strictly on your local device.

---

## ✨ Features

- 💸 **Daily Expense & Income Tracking**: Add, edit, search, and delete transactions with ease. Includes smart category suggestions.
- 📊 **Visual Analytics & Dashboard**: Category distribution donut charts, daily spending bar charts, and monthly trend graphs.
- 🏷️ **Custom Category Manager**: Tailor categories with personalized icons and color codes.
- 🔒 **Biometric & PIN Lock**: Local account protection with 4-digit PIN hashing and native fingerprint/biometric security.
- 🔔 **Scheduled Daily Reminders**: Never forget to log expenses with daily background notifications.
- 📄 **Export & Backup**: Export print-ready PDF reports, CSV balance sheets, or JSON database backups.
- 🎨 **Material 3 Customization**: Dynamic dark/light modes, customizable currency symbols, and Indian number formatting (`₹1,24,000`).

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose, Material Design 3
- **Architecture**: MVVM (ViewModel, Repository, Unidirectional Data Flow)
- **Database**: Room Database (SQLite with KSP)
- **Preferences & Security**: Jetpack DataStore Preferences, AndroidX Biometrics
- **Background Tasks**: WorkManager & AlarmManager
- **Image Loading**: Coil
- **Build System**: Gradle (Kotlin DSL), Version Catalogs (`libs.versions.toml`)

---

## 📁 Project Structure

```
spendwise/
├── .github/
│   └── workflows/
│       └── android-ci.yml          # GitHub Actions CI pipeline
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/spendwise/
│   │   │   │   ├── data/            # Room Database, DAOs, Entities, Repositories, DataStore
│   │   │   │   ├── ui/              # Screens, Dialogs, Charts, Navigation, Material3 Theme
│   │   │   │   ├── util/            # Reminders, Export (PDF/CSV/JSON), Formatters, Security
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                 # Drawables, Mipmap Icons, Values, XML Rules
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Unit & Robolectric Tests
│   │   └── androidTest/             # Android Instrumented UI Tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── art/                             # App icons, logos, and repository graphics
├── gradle/
│   └── libs.versions.toml           # Gradle Version Catalog
├── .gitignore                       # Clean Git ignore rules for Android
├── build.gradle.kts
├── README.md                        # Documentation & setup guide
└── LICENSE                          # MIT License
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- JDK 17
- Android Device or Emulator running Android 7.0 (API level 24) or higher

### Building & Running
```bash
# Clone the repository
git clone https://github.com/vishwajeetchandole/SpendWise.git
cd SpendWise

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug
```

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.

---

## 👤 Author

**Vishwajeet Chandole**
- GitHub: [@vishwajeetchandole](https://github.com/vishwajeetchandole)
