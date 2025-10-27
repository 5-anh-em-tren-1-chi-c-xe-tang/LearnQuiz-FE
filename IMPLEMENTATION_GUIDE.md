# Login & Home Screen Implementation

## 📱 Implementation Overview

This document describes the newly implemented **Login Screen** and **Home Screen** for the LearnQuiz Android application.

---

## ✅ Completed Features

### 1. **Login Screen** (`LoginActivity`)
- **Features:**
  - Username/Email and Password input fields with validation
  - Material Design TextInputLayout with icons
  - Password visibility toggle
  - Real-time error handling and display
  - Loading indicator during authentication
  - Demo user credentials display
  - MVVM architecture with LiveData

- **Demo Credentials:**
  - Username: `demo`
  - Password: `password123`

### 2. **Home Screen** (`HomeActivity`)
- **Features:**
  - Material Design toolbar
  - Instructions card with usage guide
  - Camera integration using CameraX API
  - Take photo functionality with live preview
  - Select photo from gallery
  - Interactive region selection on photos
  - Visual overlay with selection rectangle
  - Quiz generation trigger (placeholder for backend integration)
  - Loading states and error handling

---

## 🏗️ Architecture

### MVVM Pattern Implementation

```
app/src/main/java/com/example/learnquiz_fe/
├── data/
│   ├── model/
│   │   └── User.java              # User data model
│   └── repository/
│       └── AuthRepository.java    # Authentication data layer
├── ui/
│   ├── activities/
│   │   ├── LoginActivity.java     # Login screen
│   │   └── HomeActivity.java      # Home screen with camera
│   ├── viewmodel/
│   │   └── LoginViewModel.java    # Login business logic
│   └── theme/                     # Compose theme files
└── utils/                         # Helper classes (ready for expansion)
```

---

## 🎨 UI Components

### Login Screen Layout (`activity_login.xml`)
- **App title and welcome message**
- **TextInputLayout** for username/email (with email validation)
- **TextInputLayout** for password (with visibility toggle)
- **Error TextView** for displaying validation/authentication errors
- **Login MaterialButton** with loading state
- **Forgot password link** (placeholder)
- **Demo credentials info**

### Home Screen Layout (`activity_home.xml`)
- **MaterialToolbar** with app title
- **Instructions card** explaining the workflow
- **Camera preview container** with:
  - CameraX PreviewView for live camera
  - ImageView for captured/selected photos
  - Selection overlay for region marking
  - Placeholder when no image loaded
- **Action buttons:**
  - Take Photo / Capture
  - Select Photo from Gallery
  - Select Region (enabled after photo loads)
  - Generate Quiz (enabled after region selected)
- **Selection info card** displaying selected region dimensions
- **Loading overlay** for async operations

---

## 🔧 Technical Implementation

### Dependencies Added
```kotlin
// ViewModel and LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")

// CameraX for photo capture
implementation("androidx.camera:camera-camera2:1.3.4")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.camera:camera-view:1.3.4")

// ConstraintLayout for complex layouts
implementation("androidx.constraintlayout:constraintlayout:2.2.0")
```

### Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

---

## 📝 Key Classes

### **LoginViewModel.java**
- Manages authentication state using LiveData
- Validates user input (email format, password length)
- Handles async login operations
- Exposes observables for:
  - `authenticatedUser` - Successful login result
  - `errorMessage` - Validation/authentication errors
  - `isLoading` - Loading state

### **LoginActivity.java**
- Observes ViewModel LiveData
- Handles UI state changes (loading, errors)
- Navigates to HomeActivity on successful login
- Implements TextWatcher for real-time validation

### **HomeActivity.java**
- Manages camera lifecycle with CameraX
- Handles runtime permissions (Camera, Storage)
- Implements photo selection from gallery
- Provides interactive region selection with touch events
- Draws selection overlay with transparent region
- Prepares for quiz generation (backend integration point)

### **AuthRepository.java**
- Data layer for authentication
- Currently uses mock data (demo user)
- Ready for backend API integration
- Includes email validation utility

---

## 🚀 Usage Instructions

### Building and Running

1. **Set Java 17 (required for Gradle 8.13):**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   ```

2. **Build the project:**
   ```powershell
   .\gradlew clean build
   ```

3. **Install on device/emulator:**
   ```powershell
   .\gradlew installDebug
   ```

4. **Launch the app:**
   ```powershell
   adb shell am start -n com.example.learnquiz_fe/.ui.activities.LoginActivity
   ```

### User Flow

1. **Login:**
   - App launches with LoginActivity
   - Enter username: `demo` and password: `password123`
   - Click "Login" button
   - On success, navigate to HomeActivity

2. **Create Quiz from Photo:**
   - **Option A - Take Photo:**
     - Click "Take Photo"
     - Grant camera permission if prompted
     - Click "Capture" to take photo
   - **Option B - Select Photo:**
     - Click "Select Photo"
     - Choose image from gallery
   
3. **Select Region:**
   - Click "Select Region" button
   - Drag finger across the photo to mark desired area
   - Selection rectangle appears with semi-transparent overlay
   - Selected region dimensions displayed

4. **Generate Quiz:**
   - Click "Generate Quiz" button
   - (Currently shows placeholder message - ready for backend integration)

---

## 🔄 Navigation Flow

```
┌─────────────────┐
│  LoginActivity  │ (Launch Screen)
│  - Username     │
│  - Password     │
│  - Login Btn    │
└────────┬────────┘
         │ login success
         ▼
┌─────────────────┐
│  HomeActivity   │
│  - Camera       │
│  - Gallery      │
│  - Region Select│
│  - Generate Quiz│
└─────────────────┘
```

---

## 🎯 Best Practices Implemented

### ✅ Android Best Practices
- **MVVM Architecture** - Clean separation of concerns
- **LiveData** - Reactive UI updates, lifecycle-aware
- **Material Design 3** - Modern UI components
- **CameraX** - Simplified camera implementation
- **Runtime Permissions** - Proper permission handling
- **Resource Management** - All strings externalized
- **Error Handling** - Comprehensive validation and user feedback

### ✅ Code Quality
- **Well-documented** - Javadoc comments throughout
- **Readable** - Clear method names and structure
- **Modular** - Reusable components
- **Type-safe** - Proper null handling
- **Testable** - Repository pattern for easy mocking

---

## 🔮 Future Enhancements (TODOs)

### Login Screen
- [ ] Implement "Forgot Password" flow
- [ ] Add user registration
- [ ] Connect to backend authentication API
- [ ] Add biometric authentication
- [ ] Implement session persistence (SharedPreferences/DataStore)

### Home Screen
- [ ] Integrate AI/backend API for quiz generation
- [ ] Add image preprocessing (crop, enhance)
- [ ] Implement quiz result display screen
- [ ] Add photo editing tools (rotate, zoom)
- [ ] Support multiple region selections
- [ ] Save quiz history
- [ ] Add sharing functionality

### Architecture
- [ ] Add Room database for local data
- [ ] Implement Retrofit for network calls
- [ ] Add dependency injection (Hilt/Dagger)
- [ ] Implement proper state management
- [ ] Add unit and instrumentation tests

---

## 📦 File Structure

```
New Files Created:
├── data/
│   ├── model/User.java
│   └── repository/AuthRepository.java
├── ui/
│   ├── activities/
│   │   ├── LoginActivity.java
│   │   └── HomeActivity.java
│   └── viewmodel/LoginViewModel.java
└── res/
    ├── layout/
    │   ├── activity_login.xml
    │   └── activity_home.xml
    └── values/strings.xml (updated)

Modified Files:
├── app/build.gradle.kts (dependencies added)
└── AndroidManifest.xml (activities and permissions)
```

---

## 🐛 Troubleshooting

### Build Issues
**Error:** `JAVA_HOME is set to an invalid directory`
- **Solution:** Set JAVA_HOME to Java 11 or higher:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
  ```

### Runtime Issues
**Camera not working:**
- Ensure camera permission is granted in app settings
- Check device has camera hardware
- Try on physical device if emulator camera fails

**Photo selection not working:**
- Grant storage/media permissions
- Ensure device has photos in gallery

### Lint Warnings
**24 warnings found:**
- These are non-critical (missing translations, accessibility improvements)
- Can be addressed incrementally
- Use `.\gradlew lintDebug` to see full report

---

## 📚 References

- [Android CameraX Documentation](https://developer.android.com/training/camerax)
- [Material Design 3](https://m3.material.io/)
- [ViewModel & LiveData](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

---

## 👥 Demo Credentials

For testing the login functionality:
- **Username:** `demo`
- **Email:** `demo@learnquiz.com`
- **Password:** `password123`

---

**Implementation Date:** October 27, 2025  
**Android SDK:** Min 24, Target 36  
**Status:** ✅ Build Successful | Ready for Testing
