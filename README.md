# 🚨 RescueLink - Your Smart Roadside Assistance & Emergency Response App

Hey there! 👋 Welcome to **RescueLink** (formerly SOS). We've built an elegant, real-time Android emergency response and roadside assistance app. Think of it as your digital co-pilot for those unexpected moments on the road.

Built with modern Android development practices, **Jetpack Compose**, the **Firebase Suite**, and **Google Generative AI (Gemini)**, RescueLink aims to make roadside emergencies a little less stressful and a lot more manageable.

---

## 🌟 What makes RescueLink special?

1. **Intelligent Onboarding & Splash Screens**:
   - We greet you with seamless, beautiful startup animations using **Lottie**.
   - You'll see a custom transition splash (`logo2`) and a cool vehicle breakdown-themed animation (`car`). First impressions matter!

2. **Secure & Quick Access (Firebase Authentication)**:
   - A complete signup and login flow powered by **Firebase Auth**.
   - Custom user profiles are saved directly to the **Firebase Realtime Database** once you're registered.
   - We've also packed in intelligent input verification so you don't get stuck on typos.

3. **Your Emergency Command Center (The Dashboard)**:
   - **One-Tap Emergency SOS**: A high-visibility glowing red panic button that initiates our helper network dispatching when you need it most.
   - **Live Location Status Card**: Real-time mock geolocation integration showing where you are (e.g., NH48, Manesar, Haryana).
   - **Interactive Roadside Services**:
     - *Nearby Mechanics*: Find auto garages fast.
     - *Towing Services*: Prompt emergency vehicle dispatch requests.
     - *Community Help*: Coordinate with other active users nearby.
     - *Roadside Safety*: Interactive checklists and tips to keep you safe while you wait.
   - **Live Indicators**: Quick summaries of your network strength, nearby helpers, and overall safety status.

4. **Meet Tars – Your AI Safety Assistant**:
   - **Floating Interactive Assistant**: A neat slide-in overlay bubble screen giving you immediate AI-based advice.
   - Powered by the mighty **Gemini 1.5 Flash Model** using the `com.google.ai.client.generativeai` SDK.
   - Ask Tars about vehicle failure diagnostics, basic first aid, or safety navigation guidance—it’s context-aware and ready to help!

---

## 🎨 How it looks & feels

We built the entire UI using **Jetpack Compose** because we love modern, declarative design:
- **Material 3**: Expect modern colors, dynamic buttons, cards, and smooth input fields.
- **Dark Mode Aesthetic**: A custom dark mode palette (`#070A0F` base container color, high-contrast red accent for SOS actions, and clean neon secondary components). It looks sleek and saves your battery.
- **Under the hood**: We use Jetpack ViewModel to house application business logic and manage state transitions reactively. Coroutines & StateFlow manage the background heavy lifting (like chatting with Gemini) smoothly.

---

## 🛠️ The Tech Stack

- **Language**: Kotlin 💜
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Minimum SDK Support**: API Level 24 (Android 7.0 Nougat)
- **Target SDK Support**: API Level 35 (Android 15)
- **Dependency Management**: Centralized catalog via Gradle `libs.versions.toml`
- **Core Library Goodies**:
  - `firebase-auth` & `firebase-database` for identity and storage
  - `generativeai:0.9.0` to bring Gemini to life
  - `lottie-compose` for those sweet vector animations

---

## 🚀 Getting Started & Configuration

Want to take it for a spin? Here's how to get set up:

### What you need
- **Android Studio Ladybug** (or later)
- **JDK 11** or **JDK 17**
- An Android Device or Emulator running **API Level 24+**

### Firebase Integration
1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with the package name `com.example.rescuelink` (or `com.example.sos` based on your manifest).
3. Enable **Email/Password Authentication**.
4. Enable **Firebase Realtime Database** in test mode or production mode.
5. Download your `google-services.json` and pop it into the `app/` folder!

### Gemini API Key Setup
Tars the AI needs a brain! The current setup points to an API key embedded within `ChatViewModel.kt`. Let's give it your key:
1. Grab an API key from [Google AI Studio](https://aistudio.google.com/).
2. Open `app/src/main/java/com/example/rescuelink/ChatViewModel.kt` (or under `sos`).
3. Replace the `apiKey` value:
   ```kotlin
   private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
   ```

---

## 🗺️ Taking the Tour (App Flow)

1. **Splash Transition**: `MainActivity` kicks things off with a slick logo animation, then switches to a breakdown state animation.
2. **User State Gate**: We check if you're signed in via Firebase. Yes? Straight to the Dashboard. No? Off to the Login screen.
3. **Authentication Screen**: Easily toggle between Login and Register.
4. **Command Dashboard**: Tap cards for services, check your location, or hit the SOS button if things go south.
5. **AI Helper Panel**: Tap the robot face FAB at the bottom-right to chat with Tars for emergency guidance.

*Stay safe out there on the roads!* 🚗💨
