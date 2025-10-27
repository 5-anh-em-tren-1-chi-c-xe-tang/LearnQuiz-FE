---
applyTo: '**'
---
Provide project context and coding guidelines that AI should follow when generating code, answering questions, or reviewing changes.# Copilot Instructions for LearnQuiz-FE

## Project Overview
**LearnQuiz-FE** (formerly AIQuiz-FE) is an Android mobile quiz application in early development. The project is scaffolded for MVVM architecture but currently has minimal implementation.

- **Platform**: Android
- **Primary Language**: Java (with Kotlin support for Compose UI)
- **Min SDK**: 24 (Android 7.0) | **Target SDK**: 36
- **Build System**: Gradle 8.13.0 with Kotlin DSL
- **Package**: `com.example.learnquiz_fe` (note: underscore, not dot)

## Current State & Architecture

### What EXISTS Now
- **Single Activity**: `MainActivity.java` extends `AppCompatActivity`, loads `activity_main.xml`
- **XML Layout**: Basic LinearLayout with "Hello World!" TextView
- **Compose Theme**: Complete Material 3 theme setup (`Color.kt`, `Type.kt`, `Theme.kt`)
- **Theme Dual System**: 
  - XML: `@style/Theme.LearnQuiz_FE` (MaterialComponents.DayNight.NoActionBar)
  - Compose: `LearnQuiz_FETheme` with dynamic color support (Android 12+)

### What DOESN'T Exist Yet (Planned Structure)
- **No** network layer (Retrofit not added)
- **No** ViewModels, Fragments, Repositories, Adapters
- **No** data models or API services
- `data/`, `ui/activities/`, `ui/fragments/`, `ui/viewmodel/` folders **not created**

### Intended Architecture (MVVM)
When implementing features, follow this structure:
```
app/src/main/java/com/example/learnquiz_fe/
├── data/
│   ├── model/         # Entity classes (POJOs/data classes)
│   ├── network/       # Retrofit API interfaces
│   └── repository/    # Repository pattern - data access layer
├── ui/
│   ├── activities/    # Activity classes
│   ├── fragments/     # Fragment classes  
│   ├── viewmodel/     # ViewModel classes for MVVM
│   ├── adapter/       # RecyclerView adapters
│   └── theme/         # ✅ EXISTS: Compose theme (Color.kt, Type.kt, Theme.kt)
├── utils/             # Helper classes, formatters, constants
└── MainActivity.java  # ✅ EXISTS: Main entry point
```

## Tech Stack (Currently Installed)
- **AndroidX Core KTX** 1.17.0
- **Jetpack Compose BOM** 2024.09.00 (Material 3, UI tooling)
- **Lifecycle Runtime KTX** 2.9.4
- **Activity Compose** 1.11.0
- **AppCompat** 1.7.1
- **Material Components** 1.13.0
- **Testing**: JUnit 4.13.2, Espresso 3.7.0

## Build & Run (Windows PowerShell)

### Quick Start
```powershell
# Build and install on connected device/emulator
.\gradlew installDebug

# Run the app after install (use adb if needed)
adb shell am start -n com.example.learnquiz_fe/.MainActivity
```

### Common Commands
```powershell
# Clean build (fixes most build issues)
.\gradlew clean build

# Build debug APK only
.\gradlew assembleDebug
# Output: app\build\outputs\apk\debug\app-debug.apk

# Run unit tests (in app/src/test/)
.\gradlew test

# Run instrumented tests (requires device/emulator)
.\gradlew connectedAndroidTest
```

### Troubleshooting Build
```powershell
# Full rebuild with stacktrace
.\gradlew clean build --stacktrace

# Check Gradle version
.\gradlew --version

# Force dependency refresh
.\gradlew build --refresh-dependencies
```

## Coding Conventions

### Package Structure
- **Package Name**: `com.example.learnquiz_fe` (underscore is intentional)
- **Java Files**: Use package declaration `package com.example.learnquiz_fe;`
- **Kotlin Files**: Use same package structure

### Java Style (Primary Language)
- **Classes**: PascalCase (`MainActivity`, `QuizRepository`)
- **Methods**: camelCase (`onCreate`, `loadQuizData`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRIES`, `API_BASE_URL`)
- **Annotations**: Place before access modifiers
  ```java
  @Override
  public void onCreate(Bundle savedInstanceState) { ... }
  ```

### Kotlin Style (Compose UI Only)
- **Composables**: PascalCase functions with `@Composable`
  ```kotlin
  @Composable
  fun QuizScreen(modifier: Modifier = Modifier) { ... }
  ```
- **Theme Files**: See `ui/theme/` for existing patterns
  - `Color.kt`: Material color scheme values
  - `Type.kt`: Typography definitions
  - `Theme.kt`: Theme composition with dynamic color support

### XML Resources
- **Layouts**: `snake_case` (e.g., `activity_main.xml`, `item_quiz.xml`)
- **IDs**: Component prefix + name (e.g., `btn_submit`, `tv_quiz_title`, `et_answer`)
- **Strings**: Descriptive keys in `values/strings.xml` (e.g., `quiz_title`, `error_network`)
- **Colors**: Define in `values/colors.xml` OR use Compose theme colors

### Theme System (Dual Approach)
**XML Activities** use: `@style/Theme.LearnQuiz_FE` (defined in `res/values/themes.xml`)
- Parent: `Theme.MaterialComponents.DayNight.NoActionBar`
- Colors: purple_500, purple_700, teal_200
- Light status bar with dark icons

**Compose Screens** use: `LearnQuiz_FETheme { ... }` (defined in `ui/theme/Theme.kt`)
- Material 3 with dynamic color (Android 12+)
- Falls back to Purple40/Purple80 scheme on older devices

## Common Development Tasks

### 1. Adding a New Activity
```java
// 1. Create in ui/activities/QuizListActivity.java
package com.example.learnquiz_fe.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquiz_fe.R;

public class QuizListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);
    }
}
```
```xml
<!-- 2. Create res/layout/activity_quiz_list.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <!-- Your UI here -->
</LinearLayout>
```
```xml
<!-- 3. Register in AndroidManifest.xml -->
<activity
    android:name=".ui.activities.QuizListActivity"
    android:exported="false"
    android:theme="@style/Theme.LearnQuiz_FE" />
```

### 2. Adding Dependencies (Version Catalog Pattern)
```toml
# Step 1: Add to gradle/libs.versions.toml
[versions]
retrofit = "2.9.0"

[libraries]
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
```
```kotlin
// Step 2: Reference in app/build.gradle.kts
dependencies {
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
}
```
```powershell
# Step 3: Sync Gradle
.\gradlew build --refresh-dependencies
```

### 3. Creating a Compose Screen
```kotlin
// ui/screens/QuizScreen.kt
package com.example.learnquiz_fe.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.learnquiz_fe.ui.theme.LearnQuiz_FETheme

@Composable
fun QuizScreen() {
    LearnQuiz_FETheme {
        Surface {
            Text("Quiz Content")
        }
    }
}
```

## Troubleshooting

### Build Issues

#### "Execution failed for task ':app:compileDebugJavaWithJavac'"
**Symptoms**: Java compilation fails
```powershell
# Fix: Clean rebuild with diagnostic output
.\gradlew clean build --stacktrace
```
**Common causes**: 
- Package name mismatch (check `com.example.learnquiz_fe` everywhere)
- Missing imports or wrong Java version (requires Java 11)

#### "Cannot resolve symbol R" or "Unresolved reference: R"
**Symptoms**: `R.layout`, `R.id`, etc. not found
```powershell
# Fix: Rebuild project resources
.\gradlew clean
.\gradlew build
```
**Root cause**: Resource compilation failed. Check:
1. XML syntax errors in `res/` files
2. `namespace = "com.example.learnquiz_fe"` in `app/build.gradle.kts`
3. No duplicate resource IDs

#### "Gradle sync failed: Could not resolve..."
**Symptoms**: Dependency download fails
```powershell
# Fix 1: Retry with offline mode disabled
.\gradlew build --refresh-dependencies

# Fix 2: Clear Gradle cache
Remove-Item -Recurse -Force $env:USERPROFILE\.gradle\caches
.\gradlew build
```

### Runtime Issues

#### App crashes immediately after launch
**Check Logcat** (Android Studio → Logcat → filter by `learnquiz_fe`):
```
# Common crash patterns:
# 1. Theme mismatch
Error: You need to use a Theme.AppCompat theme
Fix: Ensure AndroidManifest.xml uses android:theme="@style/Theme.LearnQuiz_FE"

# 2. Activity not registered
Error: Unable to find explicit activity class
Fix: Add <activity> tag in AndroidManifest.xml

# 3. Missing permission
Error: java.lang.SecurityException
Fix: Add permission in AndroidManifest.xml (e.g., <uses-permission android:name="android.permission.INTERNET"/>)
```

#### Compose preview not rendering
**Symptoms**: "No preview found" or blank preview panel
```kotlin
// Fix: Add @Preview annotation
@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    LearnQuiz_FETheme {
        QuizScreen()
    }
}
```
Then: Build → Make Project, refresh preview

### Development Tips

#### Fast iteration cycle
```powershell
# Skip tests during development
.\gradlew installDebug -x test

# Use build cache (already enabled in gradle.properties)
# Typical build time: ~30s clean, ~5s incremental
```

#### Debugging XML layouts
- Use Android Studio Layout Inspector (Tools → Layout Inspector)
- Enable "Show Layout Bounds" in device Developer Options
- XML validation: `activity_main.xml` uses simple LinearLayout pattern

## Testing Guidelines

### Unit Tests
- **Location**: `app/src/test/java/`
- **Framework**: JUnit 4.13.2
- **Naming**: `ClassNameTest.java` or `ClassNameTest.kt`
- **Method Naming**: `methodName_scenario_expectedBehavior`

Example:
```java
@Test
public void calculateTotal_withValidItems_returnsCorrectSum() {
    // Arrange, Act, Assert
}
```

### Instrumented Tests
- **Location**: `app/src/androidTest/java/`
- **Framework**: AndroidX Test + Espresso 3.7.0
- **Use Cases**: UI tests, database tests, component integration
- **Note**: Requires emulator or physical device

## Key Files

- **`app/build.gradle.kts`**: App-level build configuration, dependencies
- **`build.gradle.kts`**: Project-level build configuration, plugin versions
- **`gradle/libs.versions.toml`**: Centralized dependency version management
- **`settings.gradle.kts`**: Project settings, repository configuration
- **`AndroidManifest.xml`**: App permissions, activities, application configuration
- **`gradle.properties`**: Gradle and Android build properties
- **`proguard-rules.pro`**: Code obfuscation rules for release builds

## Important Notes

1. **Mixed Language Support**: While the project primarily uses Java, Compose UI components are in Kotlin. Both languages coexist.

2. **Namespace**: The project uses `com.example.learnquiz_fe` (note the underscore). This must match across:
   - `app/build.gradle.kts` namespace
   - `AndroidManifest.xml` package
   - Java/Kotlin package declarations

3. **Theme Duality**: The app has both:
   - XML theme: `@style/Theme.LearnQuiz_FE` (defined in `res/values/`)
   - Compose theme: `Theme.kt` with Material 3

4. **Resource Access**: Use `R.layout`, `R.string`, `R.drawable` for XML resources. Compose resources use different access patterns.

5. **AndroidX**: Project uses AndroidX (not legacy support libraries). Always use `androidx.*` imports.

6. **Minimum SDK 24**: No need for compatibility code below Android 7.0.

## Getting Help

- **Android Documentation**: https://developer.android.com
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Material Design 3**: https://m3.material.io
- **Kotlin**: https://kotlinlang.org/docs/home.html
- **Project README**: See `README.md` for project-specific folder structure

## Quick Reference Commands

```bash
# Clean and rebuild
./gradlew clean build

# Run on device
./gradlew installDebug

# Run tests
./gradlew test connectedAndroidTest

# Check for dependency updates
./gradlew dependencyUpdates

# Generate APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

**Last Updated**: October 2025  
**Maintained by**: 5-anh-em-tren-1-chi-c-xe-tang
