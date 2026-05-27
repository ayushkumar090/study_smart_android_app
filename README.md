[Presentation Layer (Jetpack Compose / Material 3)]
│
▼
[State Management (Unidirectional Data Flow / State Hoisting)]
│
├───► [Hardware Layer (Android NotificationManager API)] ──► Auto-DND
├───► [Embedded Engine (Android WebKit WebView API)]   ──► Distraction-Free YouTube
│
▼
[Data Layer (Firebase Auth & Realtime Database Reactive Sync)]


---

## ✨ Key Features & Technical Implementations

* **🔒 Secure Cloud Authentication:** Managed via `Firebase Authentication`. Implements atomic sign-in and account registration flows with client-side syntax validation and dynamic loading states.
* **📈 Real-Time Performance Analytics:** Employs an asynchronous database observer pattern to stream user session histories into a customized state structure, updating core global metrics immediately upon transaction success.
* **🎨 Custom Canvas Data Visualization:** Bypasses heavy third-party dependency rendering by utilizing the native Jetpack Compose `Canvas` layer. Features a custom dynamic line graph to map short-term minute tracking, and a segmented horizontal proportional distribution matrix to analyze historical subject ratios.
* **📅 Responsive Streak Calendar:** Implements calendar grid logic using `java.util.Calendar` partitions to organize NoSQL database timelines into visual indicators. Displays chronological indicators for focus achievements, planned breaks, and missed targets.
* **🔇 Hardware-Level Interruption Suppression (DND):** Interacts with the Android system `NotificationManager` using strict security policy checks (`ACCESS_NOTIFICATION_POLICY`). Automatically toggles the host device into **Priority Do Not Disturb Mode** when a study block begins, and clears filters upon termination.
* **🌐 Embedded Distraction-Free Search Engine:** Wraps an Android `WebView` configured with enabled DOM storage and safe JavaScript isolation. Allows students to fetch and play YouTube tutorials inside the sandbox layout, bypassing standard algorithm-driven recommendation feeds.
* **🧪 Conditional Profile Seeding Engine:** Features a dedicated data injection hook that evaluates string matching against reviewer profiles. For testing accounts, it pre-seeds a 7-day varied mock historical study footprint instantly inside the UI state layer, providing a turnkey evaluation experience.

---

## 🛠 Technology Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3 Ecosystem)
* **Asynchronous Concurrency:** Kotlin Coroutines & Flows
* **Backend Integration:** Firebase Realtime Database & Firebase Auth
* **System Integration:** Android WebKit WebView, Android App Notification Manager
* **Architecture Design:** Unidirectional Data Flow (UDF), Functional State Hoisting

---

## 🚀 Key Architectural Challenges Overcome

### 1. Eliminating Latency in Asynchronous Cloud Seeding
**Challenge:** Early iterations encountered race conditions where database hooks read empty profiles before cloud insertion operations completed over slow connections.
**Resolution:** Developed an immediate local synchronization layer that bypasses network latency. The seeding engine injects values directly into the app's mutable UI state arrays for instantaneous rendering, while asynchronously executing a background backup save to Firebase.

### 2. Safeguarding Hardware State Consistency
**Challenge:** If a user force-closed the application or unexpectedly swiped back during an active focus block, the device's audio state risk being locked permanently in silent mode.
**Resolution:** Implemented a structural `DisposableEffect` state hook alongside the primary timer coroutine loop. This guarantees that when the screen composition leaves the active layout hierarchy under any exit condition, the hardware filter automatically defaults back to its original state:

```kotlin
DisposableEffect(Unit) {
    onDispose {
        toggleDoNotDisturb(context, enableDND = false) // Hardware safety guarantee
    }
}
🔧 Installation & Verification Setup
To compile and verify the operational lifecycle of this application locally:

Clone this repository directly within your terminal workspace:

Bash
git clone [https://github.com/ayushkumar090/Study_smart_android_app.git](https://github.com/ayushkumar090/Study_smart_android_app.git)
Open the project folder inside Android Studio (Ladybug or newer).

Synchronize your build workspace files via the Gradle build tool manager.

Deploy the application to a connected physical device or an Android Virtual Device (AVD) running API Level 33 or higher.

To test the pre-seeded analytical charts, login with the reviewer profile account credential strings (ayushkumarsingh09085@gmail.com).

👨‍💻 Developer Profile
Developer: Ayush Kumar

GitHub Profile: @ayushkumar090

Core Focus: Native Android System Engineering, Declarative UI design, Cloud Infrastructure Integration
"""

with open("README.md", "w", encoding="utf-8") as f:
f.write(readme_content)

print("File saved successfully.")

## 📱 Application Visual Gallery

To verify the visual outputs of the user interface tracking state modules, review the runtime device captures from the ecosystem below:

<p align="center">
  <img src="https://raw.githubusercontent.com/ayushkumar090/study_smart_android_app/master/Screenshot%202026-05-27%20224924.png" width="260" alt="Stats Dashboard">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/ayushkumar090/study_smart_android_app/master/Screenshot%202026-05-27%20225018.png" width="260" alt="Subject Analytics">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/ayushkumar090/study_smart_android_app/master/Screenshot%202026-05-27%20225046.png" width="260" alt="Calendar Streak View">
</p>

* **Figure 1 (Left):** Real-time monitoring metrics including compiled operational duration metrics and short-term trending line data graphs.
* **Figure 2 (Center):** Custom-drawn execution ratios allocating proportional performance matrices mapped per subject profile assignment.
* **Figure 3 (Right):** Automated evaluation layouts parsing streak achievements, structured intervals, and baseline session configurations.
