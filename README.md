# 💰 Cashiro - Expense Tracker

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg?style=flat-square" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg?style=flat-square" alt="UI">
  <img src="https://img.shields.io/badge/Database-Room-red.svg?style=flat-square" alt="Database">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License">
</div>

<p align="center">
  <strong>A modern, offline-first expense tracker built with Kotlin and Jetpack Compose</strong>
</p>

## 📱 About Cashiro

Cashiro is a comprehensive expense tracking application built purely in Kotlin with Jetpack Compose for a modern, intuitive user experience. Designed with privacy and offline functionality in mind, Cashiro helps users gain better financial awareness and develop improved budgeting skills without any cloud dependency.

## ✨ Key Features

### 🚀 Core Functionality
- **Quick Spending Entry** - Effortlessly log expenses with minimal taps
- **Smart Pattern Recognition** - Automatically identify and categorize spending patterns
- **Budget Tracking** - Set and monitor budgets with real-time progress updates
- **Trend Analysis** - Visualize spending trends with interactive charts and insights

### 🔒 Privacy & Performance
- **100% Offline** - No internet connection required, your data stays on your device
- **No Cloud Dependency** - Complete privacy with local data storage
- **Lightning Fast** - Instant access to all features without network delays

### 🎯 Benefits
- **Enhanced Spending Awareness** - Clear visualization of where your money goes
- **Improved Budgeting Skills** - Learn to manage finances effectively
- **Financial Accountability** - Stay on top of your financial goals

## 🛠️ Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Database**: Room Database (SQLite)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger
- **Asynchronous Programming**: Coroutines & Flow

## 📸 Screenshots

<!-- Add your app screenshots here -->
| Home Screen | Add Expense | Analytics | Budget Tracker |
|-------------|-------------|-----------|----------------|
| <img src="screenshots/home.png" width="200"/> | <img src="screenshots/add_expense.png" width="200"/> | <img src="screenshots/analytics.png" width="200"/> | <img src="screenshots/budget.png" width="200"/> |

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34
- Kotlin 1.9.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/RiteshKanwar/Cashiro.git
   cd Cashiro
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build and Run**
   - Wait for Gradle sync to complete
   - Click the "Run" button or press Shift + F10
   - Select your target device/emulator

## 📂 Project Structure

```
Cashiro/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cashiro/
│   │   │   │   ├── data/          # Data layer (Repository, Database, Entities)
│   │   │   │   ├── domain/        # Business logic (Use Cases, Models)
│   │   │   │   ├── presentation/  # UI layer (Compose screens, ViewModels)
│   │   │   │   ├── di/           # Dependency Injection
│   │   │   │   └── utils/        # Utility classes and extensions
│   │   │   └── res/              # Resources (layouts, strings, etc.)
│   │   └── test/                 # Unit tests
│   └── build.gradle              # App-level Gradle file
├── screenshots/                  # App screenshots
├── README.md
└── build.gradle                  # Project-level Gradle file
```

## 🏗️ Architecture

Cashiro follows the **MVVM (Model-View-ViewModel)** architecture pattern with **Clean Architecture** principles:

- **Presentation Layer**: Jetpack Compose UI + ViewModels
- **Domain Layer**: Use Cases and Business Logic
- **Data Layer**: Repository Pattern + Room Database

## 🎨 Design Principles

- **Material Design 3**: Modern, accessible, and consistent UI
- **Single Activity Architecture**: Using Navigation Compose
- **State Management**: Unidirectional data flow with Compose State
- **Reactive Programming**: Leveraging Kotlin Flows for data streams

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.


## 📋 TODO / Roadmap

- [ ] Export data to CSV/PDF

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


## 🙏 Acknowledgments

- Thanks to the Android development community
- Inspired by modern financial apps focusing on user privacy
- Built with love using Jetpack Compose

---

<div align="center">
  <p>Made with ❤️ in India</p>
</div>
