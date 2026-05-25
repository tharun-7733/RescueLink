# SOS - Smart Roadside Assistance & Emergency Response App

An elegant, real-time Android emergency response and roadside assistance application built using modern Android development practices, **Jetpack Compose**, **Firebase Suite**, and **Google Generative AI (Gemini)**.

---

## 📖 Table of Contents
- [Key Features](#-key-features)
- [Architecture & Design System](#-architecture--design-system)
- [Tech Stack](#-tech-stack)
- [Project Directory Tour](#-project-directory-tour)
- [Getting Started & Configuration](#-getting-started--configuration)
  - [Prerequisites](#prerequisites)
  - [Firebase Integration](#firebase-integration)
  - [Gemini API Key Setup](#gemini-api-key-setup)
- [App Flow](#-app-flow)

---

## ⚡ Key Features

1. **Intelligent Onboarding & Splash Screens**:
   - Seamless, beautiful startup animations using **Lottie**.
   - Includes custom transition splash (`logo2`) and vehicle breakdown-themed animations (`car`).

2. **Secure Firebase Authentication**:
   - Complete signup and login flow powered by **Firebase Auth**.
   - Custom user profiles are saved to **Firebase Realtime Database** upon successful registration.
   - Intelligent input verification and status loader UI states.

3. **Emergency Command Center (Dashboard)**:
   - **One-Tap Emergency SOS**: High-visibility glowing red panic button that initiates helper network dispatching.
   - **Live Location Status Card**: Real-time mock geolocation integration showing details like coordinate accuracy (e.g., NH48, Manesar, Haryana).
   - **Interactive Roadside Services**:
     - *Nearby Mechanics*: Quick access to auto garages.
     - *Towing Services*: Prompt emergency vehicle dispatch requests.
     - *Community Help*: Signal transmission to coordinate responses with other active users nearby.
     - *Roadside Safety*: Interactive emergency checklists and tips.
   - **Live Indicators**: Quick summaries of current network strength, number of nearby active helpers, and safety status.
   - **Recent Activity Feed**: Real-time log tracking request updates.

4. **Tars AI Safety Assistant (Gemini API Integration)**:
   - **Floating Interactive Assistant**: Floating, slide-in overlay bubble screen allowing users to get immediate AI-based advice.
   - Powered by the **Gemini 1.5 Flash Model** using the `com.google.ai.client.generativeai` SDK.
   - Context-aware support providing answers to vehicle failure diagnostics, basic first aid instructions, and safety navigation guidance.

---

## 🎨 Architecture & Design System

The application is built completely using **Jetpack Compose** following modern declarative UI guidelines:
- **Material 3**: Utilizing modern colors, dynamic buttons, cards, and input fields.
- **Dark Mode Aesthetic**: A custom dark mode palette (`#070A0F` base container color, high-contrast red accent for SOS actions, clean neon secondary components).
- **Jetpack ViewModel**: Houses application business logic and manages state transitions reactively.
- **Coroutines & StateFlow**: Manages background asynchronous processes (e.g., calling the Gemini API, delaying splash transitions) smoothly.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Minimum SDK Support**: API Level 24 (Android 7.0 Nougat)
- **Target SDK Support**: API Level 35 (Android 15)
- **Dependency Versioning**: Centralized catalog via Gradle `libs.versions.toml`
- **Core Library Dependencies**:
  - `com.google.firebase:firebase-auth` (Authentication)
  - `com.google.firebase:firebase-database` (Realtime Database profile storage)
  - `com.google.ai.client.generativeai:generativeai:0.9.0` (Google Generative AI SDK)
  - `com.airbnb.android:lottie-compose:6.6.0` (Lottie vector animation support)
  - `androidx.compose.material:material-icons-extended` (Material 3 design icon resources)

---

## 📂 Project Directory Tour

Below is the directory mapping for the core components:

```text
SOS/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml   # Manifest outlining application nodes and permissions
│   │       ├── java/com/example/sos/
│   │       │   ├── MainActivity.kt   # App launcher, navigation host, & splash handlers
│   │       │   ├── DashboardScreen.kt# Emergency dashboard UI & floating AI helper panel
│   │       │   ├── ChatViewModel.kt   # Business logic layer for interacting with Gemini API
│   │       │   └── ui/theme/
│   │       │       ├── Color.kt      # App specific palette definitions
│   │       │       ├── Theme.kt      # Custom MaterialTheme setup wrapper (SOSTheme)
│   │       │       ├── Type.kt       # Typography configurations
│   │       │       └── login.kt      # Compose based authentication screen logic
│   │       └── res/
│   │           └── raw/
│   │               ├── logo2.json    # Initial splash animation
│   │               ├── car.json      # Interstitial loading animation
│   │               └── login.json    # Auth illustration vector animation
└── gradle/
    └── libs.versions.toml             # Gradle version catalog for library dependency management
```

---

## 🚀 Getting Started & Configuration

### Prerequisites
- **Android Studio Ladybug** (or later)
- **JDK 11** or **JDK 17**
- An Android Device or Emulator running **API Level 24+**

### Firebase Integration
1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with the package name `com.example.sos`.
3. Enable **Email/Password Authentication** in the Firebase console authentication panel.
4. Enable **Firebase Realtime Database** in test mode or production mode (with proper write/read rules).
5. Download your project's configuration file `google-services.json` and place it in the `app/` folder:
   ```text
   SOS/app/google-services.json
   ```

### Gemini API Key Setup
The AI assistant runs on **Gemini 1.5 Flash**. The current setup points to an API key embedded within `ChatViewModel.kt`.
To configure your own key:
1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. Open `ChatViewModel.kt` located in `app/src/main/java/com/example/sos/ChatViewModel.kt`.
3. Replace the `apiKey` value on line 14:
   ```kotlin
   private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
   ```

---

## 🔄 App Flow

1. **Splash Transition**:
   - `MainActivity` launches with `logo2` animation on a black background for 3 seconds.
   - Instantly switches to the `car` animation for 4 seconds representing a breakdown state.
2. **User State Gate**:
   - Checks Firebase Auth for an active session.
   - If a user is already signed in, they bypass login and land on the `Dashboard`.
   - If not signed in, they are redirected to the `LoginScreen`.
3. **Authentication Screen**:
   - Toggle between **Login** and **Register**.
   - Successful signup writes registration logs to the Firebase Realtime Database path `users/<userId>` with the matching username and email.
4. **Command Dashboard**:
   - Tap cards to request roadside services, view local statistics, or inspect live mock coordinate points.
   - Interact with the bottom bar to view status details or trigger the immediate SOS warning.
5. **AI Helper Panel**:
   - Click the robot face Floating Action Button (FAB) at the bottom-right.
   - Type queries inside the chat window and receive real-time, context-rich emergency guidance.
