# 🌿 Just Relax – Sound Mixing & Relaxation App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Kotlin%20Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-KMP-7f52ff.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Jetpack%20Compose](https://img.shields.io/badge/Jetpack%20Compose-Multiplatform-3ddc84.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVI%20Inspired-orange.svg)](https://developer.android.com/topic/architecture)
[![Design](https://img.shields.io/badge/Design-Feature%20Based%20Modules-purple.svg)](https://developer.android.com/guide/app-bundle/play-feature-delivery)

> A modern **Kotlin Multiplatform** relaxation app where users can mix ambient sounds, create custom soundscapes, and relax with a minimal, soft-UI driven experience.

**▶️ Google Play:** https://play.google.com/store/apps/details?id=com.mustafakoceerr.justrelax

---

## ✨ Highlights

- 🎧 Custom ambient sound mixer  
- 🧠 AI-assisted sound mix suggestions  
- ⏱️ Sleep & focus timer  
- 💾 Save & manage favorite mixes  
- 🌗 Light / Dark theme support  
- 🌍 Kotlin Multiplatform-ready architecture (Android & iOS)

---

📽️ Feature Demos
<details> <summary><b>🏠 Home & 🧠 AI Suggestions (Click to expand)</b></summary> <p align="center"> <img src="assets/home_demo.gif" width="190" /> <img src="assets/ai_demo.gif" width="190" />


<em>Explore ambient sounds and generate AI-driven mixes.</em> </p> </details>

<details> <summary><b>🎚️ Mixer & ⏱️ Timer (Click to expand)</b></summary> <p align="center"> <img src="assets/mixer_demo.gif" width="190" /> <img src="assets/timer_demo.gif" width="190" />


<em>Fine-tune your layers and set a sleep timer for your session.</em> </p> </details>

<details> <summary><b>💾 Saved Mixes & ⚙️ Settings (Click to expand)</b></summary> <p align="center"> <img src="assets/saved_demo.gif" width="190" /> <img src="assets/settings_demo.gif" width="190" />


<em>Manage your favorite soundscapes and customize your app experience.</em> </p> </details>



## 🖼️ App Gallery

### 📱 Light Theme (8)

<table>
  <tr>
    <th>Splash</th>
    <th>Home</th>
    <th>AI</th>
    <th>AI (Alt)</th>
  </tr>
  <tr>
    <td><img src="assets/JR_light_splash_1.png" height="380"/></td>
    <td><img src="assets/JR_light_home_1.png" height="380"/></td>
    <td><img src="assets/JR_light_ai_1.png" height="380"/></td>
    <td><img src="assets/JR_light_ai_2.png" height="380"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>Mixer</th>
    <th>Timer</th>
    <th>Saved</th>
    <th>Settings</th>
  </tr>
  <tr>
    <td><img src="assets/JR_light_mixer_2.png" height="380"/></td>
    <td><img src="assets/JR_light_timer_2.png" height="380"/></td>
    <td><img src="assets/JR_light_saved_2.png" height="380"/></td>
    <td><img src="assets/JR_light_settings_1.png" height="380"/></td>
  </tr>
</table>

---

### 🌙 Dark Theme (8)

<table>
  <tr>
    <th>Onboarding</th>
    <th>Home</th>
    <th>AI</th>
    <th>AI (Alt)</th>
  </tr>
  <tr>
    <td><img src="assets/JR_dark_onboarding_1.png" height="380"/></td>
    <td><img src="assets/JR_dark_home_1.png" height="380"/></td>
    <td><img src="assets/JR_dark_ai_1.png" height="380"/></td>
    <td><img src="assets/JR_dark_ai_2.png" height="380"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>Mixer</th>
    <th>Timer</th>
    <th>Saved</th>
    <th>Settings</th>
  </tr>
  <tr>
    <td><img src="assets/JR_dark_mixer_2.png" height="380"/></td>
    <td><img src="assets/JR_dark_timer_2.png" height="380"/></td>
    <td><img src="assets/JR_dark_saved_2.png" height="380"/></td>
    <td><img src="assets/JR_dark_settings_1.png" height="380"/></td>
  </tr>
</table>

---

## 🛠 Tech Stack & Tooling

> Main technologies used in this project, with versions and a short purpose note.

### Core Language & Platform
- **Kotlin Multiplatform** `2.2.21` — shared business logic across Android & iOS  
- **Android Gradle Plugin** `8.11.0` — modern Android build tooling  
- **Compile SDK / Target SDK** `36` · **Min SDK** `29` — modern API support with sensible backward compatibility  

### UI
- **Compose Multiplatform** `1.9.3` — declarative UI  
- **Material Design 3** — consistent design system  
- **AndroidX Activity Compose** `1.11.0` — Compose-first integration  

### State & Concurrency
- **Kotlin Coroutines** `1.10.2` — async + structured concurrency  
- **Flow** — reactive state streams  

### Navigation
- **Voyager** `1.1.0-beta03` — KMP-friendly navigation (lightweight & clean)  

### Dependency Injection
- **Koin** `4.1.1` — Kotlin-first DI with Compose support  

### Networking & Serialization
- **Ktor Client** `3.3.2` — multiplatform HTTP client  
- **Kotlinx Serialization** `1.9.0` — type-safe JSON parsing  

### Persistence
- **SQLDelight** `2.2.1` — type-safe multiplatform database  
- **Multiplatform Settings** `1.3.0` — shared key-value storage  

### Media & Assets
- **Media3 / ExoPlayer** `1.8.0` — audio playback engine  
- **Coil 3** `3.3.0` — image loading for Compose  
- **Okio** `3.16.2` — filesystem & I/O utilities  

---

## 🧩 Architecture (Short)

- Feature-based modular structure (`feature/*`)
- Shared core building blocks (`core/*`)
- MVI-inspired state management
- Clear separation of UI / Domain / Data

```text
.
├── build-logic/
├── composeApp/
├── iosApp/
├── core/
└── feature/
```

---

### 🛠 build-logic (Convention Plugins)

The `build-logic` module contains **custom Gradle convention plugins** that standardize configuration across modules, so each new module stays consistent with minimal Gradle boilerplate.

What it standardizes (high-level):
- **Shared Android config:** common `compileSdk / minSdk / targetSdk` defaults  
- **Kotlin / KMP setup:** consistent compiler options and multiplatform target setup  
- **Compose defaults:** Compose Multiplatform setup applied consistently  
- **Common dependency patterns:** repeatable “baseline” dependencies per module type  
- **Build features:** centralized toggles and shared build settings (so modules don’t drift over time)

---

## 🚧 Roadmap

- More advanced AI-driven personalization

---

## 📄 License

Licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.

## 🔔 Notice

See the [NOTICE](NOTICE) file for attribution and additional information.
