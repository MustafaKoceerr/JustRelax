# 🌿 Just Relax – Sound Mixing & Relaxation App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Kotlin%20Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-KMP-7f52ff.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Jetpack%20Compose](https://img.shields.io/badge/Jetpack%20Compose-Multiplatform-3ddc84.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVI%20Inspired-orange.svg)](https://developer.android.com/topic/architecture)
[![Design](https://img.shields.io/badge/Design-Feature%20Based%20Modules-purple.svg)](https://developer.android.com/guide/app-bundle/play-feature-delivery)

> A modern **Kotlin Multiplatform** relaxation app that allows users to mix ambient sounds, create custom soundscapes, and relax with a minimal, soft-UI driven experience.

---

## ✨ Key Highlights

- 🎧 Custom ambient sound mixer  
- 🧠 AI-assisted sound mix suggestions  
- ⏱️ Sleep & focus timer  
- 💾 Save & manage favorite mixes  
- 🌗 Light / Dark theme support  
- 🌍 Multiplatform-ready architecture (Android & iOS)

---

## 🛠 Tech Stack & Tooling

> Below is a concise overview of the main technologies used in this project, along with **why** they were chosen.

### Core Language & Platform
- **Kotlin Multiplatform** `2.2.21`  
  Shared business logic across Android & iOS with a single codebase.
- **Android Gradle Plugin** `8.11.0`  
  Latest tooling for modern Android builds.
- **Compile SDK / Target SDK** `36` · **Min SDK** `29`  
  Modern Android API support with reasonable backward compatibility.

### UI
- **Compose Multiplatform** `1.9.3`  
  Declarative UI for Android and shared UI foundations.
- **Material Design 3**  
  Consistent, modern, and accessible design language.
- **AndroidX Activity Compose** `1.11.0`  
  Compose-first activity integration.

### State & Concurrency
- **Kotlin Coroutines** `1.10.2`  
  Structured concurrency and async task handling.
- **Flow**  
  Reactive, observable state streams across the app.

### Navigation
- **Voyager** `1.1.0-beta03`  
  Multiplatform-friendly navigation solution, chosen for its simplicity and KMP maturity.

### Dependency Injection
- **Koin** `4.1.1`  
  Lightweight, Kotlin-first dependency injection with excellent Compose support.

### Networking & Serialization
- **Ktor Client** `3.3.2`  
  Multiplatform HTTP client used for API communication.
- **Kotlinx Serialization** `1.9.0`  
  Type-safe JSON parsing across platforms.

### Persistence
- **SQLDelight** `2.2.1`  
  Type-safe, multiplatform database solution.
- **Multiplatform Settings** `1.3.0`  
  Simple key-value storage shared between platforms.

### Media & Assets
- **Media3 / ExoPlayer** `1.8.0`  
  Audio playback engine for ambient sounds.
- **Coil 3** `3.3.0`  
  Modern, coroutine-based image loading for Compose.
- **Okio** `3.16.2`  
  File system and I/O utilities.

---

## 📸 App Gallery

### 📱 Light Theme Screens

<table>
  <tr>
    <th>Home</th>
    <th>Home (Alt)</th>
    <th>Mixer</th>
    <th>Mixer (Alt)</th>
  </tr>
  <tr>
    <td><img src="assets/light_home_1.jpg" height="380"/></td>
    <td><img src="assets/light_home_2.jpg" height="380"/></td>
    <td><img src="assets/light_mixer_1.jpg" height="380"/></td>
    <td><img src="assets/light_mixer_2.jpg" height="380"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>AI Suggestions</th>
    <th>AI Suggestions (Alt)</th>
    <th>Saved Mixes</th>
    <th>Saved Mixes (Alt)</th>
  </tr>
  <tr>
    <td><img src="assets/light_ai_1.jpg" height="380"/></td>
    <td><img src="assets/light_ai_2.jpg" height="380"/></td>
    <td><img src="assets/light_saved_1.jpg" height="380"/></td>
    <td><img src="assets/light_saved_2.jpg" height="380"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>Timer</th>
    <th>Timer (Alt)</th>
    <th>Settings</th>
    <th>Settings (Alt)</th>
  </tr>
  <tr>
    <td><img src="assets/light_timer_1.jpg" height="380"/></td>
    <td><img src="assets/light_timer_2.jpg" height="380"/></td>
    <td><img src="assets/light_settings_1.jpg" height="380"/></td>
    <td><img src="assets/light_settings_2.jpg" height="380"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>Loading</th>
    <th>Coming Soon</th>
    <th>Coming Soon</th>
    <th>Coming Soon</th>
  </tr>
  <tr>
    <td><img src="assets/light_loading_1.jpg" height="380"/></td>
    <td align="center"><i>New states planned</i></td>
    <td align="center"><i>New states planned</i></td>
    <td align="center"><i>New states planned</i></td>
  </tr>
</table>

---

### 📱 Dark Theme (Preview)

<table>
  <tr>
    <th>Settings (Dark)</th>
    <th>Palette Update</th>
    <th>Palette Update</th>
    <th>Palette Update</th>
  </tr>
  <tr>
    <td><img src="assets/dark_settings_1.jpg" height="380"/></td>
    <td align="center"><i>Color palette refinement in progress</i></td>
    <td align="center"><i>Contrast tuning planned</i></td>
    <td align="center"><i>Additional screens coming soon</i></td>
  </tr>
</table>

---

## 🎥 Feature Demo

Short demo showcasing the overall flow and UX of the application.

- 📹 **Video Demo:** [Watch Just Relax App Preview on YouTube](https://youtube.com/shorts/WNjl-RuWqtQ)

---

## 🧱 Architecture Overview

- MVI-inspired state management  
- Clear separation of **UI / Domain / Data**  
- Feature-based modular structure  
- Shared business logic across platforms  

Designed with **scalability**, **testability**, and **long-term maintainability** in mind.

---

## 🚧 Known Limitations & Future Improvements

- Extended offline caching strategies  
- Further refinement of dark theme palette  
- Advanced AI-driven personalization  

This project is actively evolving and serves as both a learning-focused and production-ready foundation.

---

⭐ *This repository is crafted to demonstrate modern Android & Kotlin Multiplatform development practices with a strong emphasis on architecture, UI consistency, and thoughtful technology choices.*
