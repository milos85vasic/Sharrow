# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ShareConnect is an Android application that serves as a media sharing hub, allowing users to share links from various streaming services (YouTube, Vimeo, Twitch, etc.) to local download services including MeTube, YT-DLP, torrent clients, and jDownloader.

**Key Features:**
- Multi-service support (MeTube, YT-DLP, qBittorrent, Transmission, uTorrent, jDownloader)
- Universal media sharing from 10+ platforms
- Multiple service profiles with default selection
- Encrypted SQLCipher database storage
- Material Design 3 UI with 6 customizable themes
- Comprehensive history tracking and management
- System app integration for direct sharing

## Project Architecture

### Module Structure

The project follows a modular architecture with two main components:

#### 1. Application Module (`/Application/`)
- **Purpose**: Main Android application containing UI, business logic, and data layers
- **Package**: `com.shareconnect`
- **Build**: Standard Android application module with Kotlin and Compose support
- **Key Components**:
  - Activities: MainActivity, ShareActivity, SettingsActivity, ProfilesActivity, etc.
  - Database: Room database with SQLCipher encryption
  - Managers: ProfileManager, ThemeManager
  - API Client: ServiceApiClient for service communication

#### 2. Toolkit Module (`/Toolkit/`)
- **Purpose**: Reusable Android development toolkit (git submodule)
- **Submodules**:
  - `Main`: Core abstractions, networking, UI components, database utilities
  - `Test`: Testing utilities and common test infrastructure
  - `Analytics`: Analytics and crash reporting components
  - `JCommons`: Java common utilities and obfuscation tools

### Data Architecture

**Database Layer (SQLCipher + Room):**
- `HistoryDatabase`: Main database class with version 3 schema
- `HistoryItem`: Entity for tracking shared links with rich metadata (title, description, thumbnail)
- `Theme`: Entity for theme preferences storage
- `ServerProfileEntity`: Entity for service profiles with authentication
- `HistoryItemDao`, `ThemeDao`, `ServerProfileDao`: Data access objects
- `HistoryRepository`, `ThemeRepository`, `ServerProfileRepository`: Repository pattern implementations

**Key Data Models:**
- `ServerProfile`: Service configuration with authentication support (username/password)
- `HistoryItem`: Shared content tracking with rich metadata (title, description, thumbnail URL)
- `Theme`: UI theme configuration with color schemes and dark/light modes
- `UrlMetadata`: Fetched metadata for shared URLs

### Security & Encryption

- **SQLCipher**: All local data encrypted at rest using SQLCipher 4.10.0+
- **Room Database**: Profiles migrated from SharedPreferences to encrypted Room database
- **Salt Management**: Application-specific salt for encryption keys
- **Authentication**: Secure storage of credentials with proper API implementations:
  - qBittorrent: Cookie-based authentication
  - Transmission: Session ID handling
  - jDownloader: My.JDownloader API support
- **No External Data**: All data remains on device unless explicitly shared by user

## Build System & Configuration

### Gradle Structure

**Root Level (`/build.gradle`):**
```
- Kotlin version: 2.2.20
- ShareConnect version: 1.0.0 (version code 2)
- Toolkit version: 2.0.3
- Android Gradle Plugin: 8.13.0
- Multiple Maven repositories (including Chinese mirrors for better accessibility)
```

**Application Module (`/Application/build.gradle`):**
```
- compileSdk: 36, targetSdk: 36, minSdk: 28
- Supports signing configs for development and cloud deployment
- Environment variable configuration (.env file support)
- Comprehensive testing dependencies (JUnit, Espresso, Mockito, Robolectric)
- Firebase integration (Crashlytics, Analytics, App Distribution)
- Room KSP code generation
```

**Toolkit Module (`/Toolkit/build.gradle`):**
```
- Shared build configuration for toolkit modules
- Obfuscation support via JCommons
- Proxy server configuration for development/testing
- Extensive dependency management (Retrofit, Room, Compose, Firebase)
- SQLCipher 4.10.0+ with 16KB page size support
```

## Development Commands

### Building
```bash
# Clean and build debug APK
./build_app.sh

# Manual Gradle commands
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
```

### Testing
```bash
# Run all tests (unit + instrumentation + automation)
./run_all_tests.sh

# Individual test types
./run_unit_tests.sh             # Business logic tests
./run_instrumentation_tests.sh  # Android component tests
./run_automation_tests.sh        # E2E workflow tests

# Emulator functionality testing
./test_emulator_functionality.sh

# Run single test class or method
./gradlew test --tests "com.shareconnect.ProfileManagerTest"
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.shareconnect.DatabaseMigrationTest
```

### Linting & Code Quality
```bash
# Run lint checks
./gradlew lint

# Run detekt for Kotlin code analysis
./gradlew detekt

# Check dependencies
./gradlew dependencies
```

### Other Utilities
```bash
./assemble     # Quick assembly script
./commit       # Git commit helper
./distribute   # Distribution helper
```

## Testing Framework

ShareConnect has a comprehensive 3-tier testing strategy:

### 1. Unit Tests
- **Location**: `Application/src/test/kotlin/`
- **Purpose**: Test business logic, data models, utilities
- **Coverage**: ServerProfile, ProfileManager, Theme, HistoryItem, DialogUtils, ServiceApiClient
- **Framework**: JUnit 4, Mockito, Robolectric

### 2. Instrumentation Tests
- **Location**: `Application/src/androidTest/kotlin/`
- **Purpose**: Test Android components requiring device context
- **Coverage**: Database operations, Activity lifecycle, UI components
- **Framework**: AndroidJUnit, Espresso, Room testing

### 3. Automation Tests
- **Location**: `Application/src/androidTest/kotlin/com/shareconnect/automation/`
- **Purpose**: End-to-end user workflows and accessibility compliance
- **Coverage**: Complete app flows, accessibility validation, stress testing
- **Framework**: UIAutomator, Espresso, Accessibility Testing Framework

### Test Reporting
- **Location**: `Documentation/Tests/YYYYMMDD_HHMMSS_TEST_ROUND/`
- **Formats**: HTML reports, JUnit XML, execution logs, screenshots
- **CI/CD Ready**: Exit codes and XML reports for pipeline integration

## Key Architectural Patterns

### 1. Repository Pattern
- `HistoryRepository` and `ThemeRepository` abstract data access
- Separation of data sources from business logic
- Testable and mockable data layer

### 2. Manager Pattern
- `ProfileManager`: Handles service profile operations
- `ThemeManager`: Manages theme switching and persistence
- Centralized business logic with clear responsibilities

### 3. Activity-Based Navigation
- Traditional Android Activities for main screens
- Intent-based navigation between screens
- Share intent handling for external app integration

### 4. Material Design 3
- Consistent UI components following Material Design guidelines
- Theme system with 6 color schemes (Warm Orange, Crimson, Light Blue, Purple, Green, Material)
- Dynamic theming with light/dark mode support

## File Organization

### Application Source Structure
```
Application/src/main/kotlin/com/shareconnect/
├── Activities (MainActivity, ShareActivity, SettingsActivity, etc.)
├── Adapters (ProfileAdapter, HistoryAdapter, ThemeAdapter)
├── Database (HistoryDatabase, DAOs, Entities, Repositories)
├── Managers (ProfileManager, ThemeManager)
├── Models (ServerProfile)
├── Utils (DialogUtils, ServiceApiClient)
└── SCApplication (Application class extending BaseApplication)
```

### Configuration Files
- `/Application/src/main/AndroidManifest.xml`: App permissions, activities, intent filters
- `/.env`: Environment variables for signing and configuration
- `/gradle.properties`: Gradle configuration
- `/local.properties`: Local development settings

### Resource Management
- Adaptive icons and splash screens
- Multi-language support structure
- Theme-aware resources

## Integration Points

### External Services
- **MeTube**: Self-hosted YouTube downloader integration
- **YT-DLP**: Direct integration for media downloading
- **Torrent Clients**: qBittorrent, Transmission, uTorrent web UI support
- **jDownloader**: Web interface integration

### Android System Integration
- Share intent filters for supported media platforms
- Clipboard URL detection and sharing
- System app sharing capabilities
- Adaptive launcher icons

## Development Workflow

### Environment Setup
1. Clone repository with submodules: `git clone --recursive`
2. Configure signing keys in `.env` file (see `SIGNING_SETUP.md`)
3. Set up Android SDK with API level 36
4. Configure development environment variables

### Development Process
1. Use appropriate test scripts during development
2. Follow Material Design 3 guidelines for UI changes
3. Maintain database migrations for schema changes
4. Update documentation for architectural changes
5. Ensure accessibility compliance for UI modifications

### Release Process
1. Run comprehensive test suite (`./run_all_tests.sh`)
2. Update version numbers in root `build.gradle`
3. Build release APK with proper signing
4. Validate functionality on multiple devices/Android versions

## Common Tasks for Claude Code

### Adding New Features
1. **Database Changes**: Update entities, DAOs, and repositories in `/Application/src/main/kotlin/com/shareconnect/database/`
2. **UI Changes**: Follow Material Design 3 patterns, update themes if needed
3. **New Services**: Extend `ServiceApiClient` and add service profiles
4. **Testing**: Add corresponding unit, instrumentation, and automation tests

### Debugging & Analysis
1. **Build Issues**: Check Gradle configuration and dependency conflicts
2. **Database Issues**: Review Room entities and migration strategies
3. **UI Issues**: Verify theme consistency and Material Design compliance
4. **Test Failures**: Use comprehensive test reports in `Documentation/Tests/`

### Code Quality & Maintenance
1. **Dependencies**: Review and update versions in Toolkit and Application modules
2. **Security**: Ensure SQLCipher usage and proper data encryption
3. **Performance**: Monitor APK size and runtime performance
4. **Accessibility**: Maintain compliance using automation test framework

## Important Notes

- **Toolkit Dependency**: The Toolkit is a git submodule; changes there affect multiple projects
- **Environment Variables**: Many configurations depend on `.env` file setup
- **Signing**: Release builds require proper keystore configuration
- **Testing**: Always run full test suite before significant changes
- **Database**: All data is encrypted; backup/restore requires proper key management

This guide provides Claude Code instances with the essential knowledge to effectively work with the ShareConnect codebase while maintaining its architectural integrity and development standards.