# CoFinance

**CoFinance** is a personal finance companion app designed to help you understand your daily money habits in a simple, structured, and engaging way. Unlike complex banking apps, CoFinance is a lightweight tool focused on manual tracking, goal setting, and spending insights for everyday use.

## 🚀 Key Features

- **Transaction Tracking**: Easily record and categorize your daily expenses and income.
- **Spending Insights**: Visualize your financial habits with intuitive charts and patterns.
- **Goal Monitoring**: Set and track simple savings goals to stay on top of your financial progress.
- **Dark Mode Support**: Full support for system-wide and manual Dark/Light theme switching.
- **Offline First Behavior**: 100% functional without internet; data is stored locally for maximum privacy and zero latency.
- **Home Dashboard**: A quick overview of your current financial status, including monthly savings goals and alerts.
- **Privacy Focused**: All data stays on your device.

## 🛠️ Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) (Material 3)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: Clean Architecture with MVVM (Model-View-ViewModel)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room) (Offline-first)
- **Local Storage**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Charts**: [Vico](https://github.com/patrykandpatrick/vico)
- **Navigation**: Jetpack Compose Navigation

## 📂 Project Structure

The project follows the **Clean Architecture** pattern to ensure scalability and maintainability:

```text
app/src/main/java/com/finance/app/
├── data/          # Implementation of repositories, Room DB, DAOs, and Entities
├── domain/        # Business logic: Repository interfaces, Use Cases, and Domain Models
├── ui/            # UI Layer: Screens, ViewModels, Composable Components, and Theme
├── di/            # Dependency Injection modules (Hilt)
├── navigation/    # Navigation graph and route definitions
└── utils/         # Helper functions and extensions
```

## 📶 Offline-First Design

CoFinance is built with an **offline-first** approach. It allows you to:
- Use the app anywhere without an internet connection.
- Ensure your financial data never leaves your device (Maximum Privacy).
- Experience zero-latency interactions as all data is served from the local Room database.

## ⚙️ Setup Instructions

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11+
- Android Device/Emulator running API 26+ (Android 8.0)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/jyotirmay0/CoFinance.git
   ```
2. Open the project in **Android Studio**.
3. Wait for the Gradle sync to finish.
4. Click **Run** to install the app on your device or emulator.

## 📝 Assumptions & Limitations

- **Manual Entry**: This app does NOT sync with bank accounts. It relies on user input for accuracy.
- **No Cloud Backup**: Currently, data is stored only on the local device. Uninstalling the app will remove all data unless cleared via system settings.
- **Offline Only**: The app does not require a network to function, ensuring high privacy but no cross-device synchronization.

## 🎨 Design Philosophy

CoFinance is designed to feel **personal and structured**. It uses a modern, mobile-friendly interface with:
- Compact UI components for maximum information density without clutter.
- Vibrant but professional color palettes.
- Smooth transitions and micro-animations for an immersive experience.

---

*Build your better money habits with CoFinance.*
