# ShareConnect - Qwen Code Context

This document provides essential context for Qwen Code when working with the ShareConnect repository. It outlines the project's purpose, architecture, build system, and development workflows.

## Project Overview

ShareConnect is an Android application that serves as a media sharing hub, allowing users to share links from various streaming services (YouTube, Vimeo, Twitch, Reddit, Twitter, Instagram, Facebook, SoundCloud, etc.) to local download services including:

- MeTube
- YT-DLP
- Torrent clients (qBittorrent, Transmission, uTorrent)
- jDownloader

### Key Features

- Multi-service support with service profiles
- Universal media sharing from 10+ platforms
- Encrypted SQLCipher database storage
- Material Design 3 UI with 6 customizable themes
- Comprehensive history tracking and management
- System app integration for direct sharing
- Clipboard URL sharing
- Connection testing capabilities
- Optional username and password authentication for protected services

## Project Architecture

### Module Structure

The project follows a modular architecture with two main components:

#### 1. Application Module (`/Application/`)
- Main Android application containing UI, business logic, and data layers
- Package: `com.shareconnect`
- Standard Android application module with Kotlin and modern Android components

#### 2. Toolkit Module (`/Toolkit/`)
- Reusable Android development toolkit (git submodule)
- Contains multiple submodules:
  - `Main`: Core abstractions, networking, UI components, database utilities
  - `Test`: Testing utilities and common test infrastructure
  - `Analytics`: Analytics and crash reporting components
  - `JCommons`: Java common utilities and obfuscation tools

### Data Architecture

#### Database Layer (SQLCipher + Room)
- `HistoryDatabase`: Main database class
- `HistoryItem`: Entity for tracking shared links with metadata
- `Theme`: Entity for theme preferences storage
- `HistoryItemDao` & `ThemeDao`: Data access objects
- `HistoryRepository` & `ThemeRepository`: Repository pattern implementations

#### Key Data Models
- `ServerProfile`: Service configuration for different download services
- `HistoryItem`: Shared content tracking with timestamps and service info
- `Theme`: UI theme configuration with color schemes and dark/light modes

### Security & Encryption
- All local data encrypted at rest using SQLCipher
- Application-specific salt for encryption keys
- No external data collection without explicit user action

## Build System & Configuration

### Gradle Structure
- Kotlin version: 2.2.20
- ShareConnect version: 1.0.0 (version code 2)
- Toolkit version: 2.0.3
- Android Gradle Plugin: 8.13.0
- Multiple Maven repositories (including Chinese mirrors for better accessibility)
- compileSdk: 36, targetSdk: 36, minSdk: 28

### Environment Configuration
- Supports `.env` file for signing configuration
- Development and cloud signing configurations
- Firebase integration (Crashlytics, Analytics, App Distribution)

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

### Testing Infrastructure
```bash
# Run all tests (unit + instrumentation + automation)
./run_all_tests.sh

# Individual test types
./run_unit_tests.sh           # Business logic tests
./run_instrumentation_tests.sh  # Android component tests
./run_automation_tests.sh       # E2E workflow tests
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
- Location: `Application/src/test/kotlin/`
- Framework: JUnit 4, Mockito, Robolectric
- Coverage: Business logic, data models, utilities

### 2. Instrumentation Tests
- Location: `Application/src/androidTest/kotlin/`
- Framework: AndroidJUnit, Espresso, Room testing
- Coverage: Android components requiring device context

### 3. Automation Tests
- Location: `Application/src/androidTest/kotlin/com/shareconnect/automation/`
- Framework: UIAutomator, Espresso, Accessibility Testing Framework
- Coverage: End-to-end user workflows and accessibility compliance

## Key Architectural Patterns

### 1. Repository Pattern
- `HistoryRepository` and `ThemeRepository` abstract data access
- Separation of data sources from business logic

### 2. Manager Pattern
- `ProfileManager`: Handles service profile operations
- `ThemeManager`: Manages theme switching and persistence

### 3. Activity-Based Navigation
- Traditional Android Activities for main screens
- Intent-based navigation between screens
- Share intent handling for external app integration

### 4. Material Design 3
- Consistent UI components following Material Design guidelines
- Theme system with 6 color schemes
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
│   └── ServerProfile: Contains id, name, url, port, serviceType, torrentClientType, username, password (optional fields for authentication)
├── Utils (DialogUtils, ServiceApiClient)
└── SCApplication (Application class extending BaseApplication)
```

## Integration Points

### 3. New Services
- MeTube: Self-hosted YouTube downloader integration
- YT-DLP: Direct integration for media downloading
- Torrent Clients: qBittorrent, Transmission, uTorrent web UI support
- jDownloader: Web interface integration

### Authentication Support
- Username and Password: Optional authentication fields in ServerProfile
- ServiceApiClient: Updated to include basic authentication in API requests
- Security: Credentials stored with same SQLCipher encryption as other data
- Implementation: Basic authentication added for all service types (MeTube, YT-DLP, Torrent clients, jDownloader)

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

## Common Tasks for Qwen Code

### Adding New Features
1. Database Changes: Update entities, DAOs, and repositories in the database package
2. UI Changes: Follow Material Design 3 patterns, update themes if needed
3. New Services: Extend `ServiceApiClient` and add service profiles
4. Testing: Add corresponding unit, instrumentation, and automation tests

### Debugging & Analysis
1. Build Issues: Check Gradle configuration and dependency conflicts
2. Database Issues: Review Room entities and migration strategies
3. UI Issues: Verify theme consistency and Material Design compliance
4. Test Failures: Use comprehensive test reports in `Documentation/Tests/`

### Code Quality & Maintenance
1. Dependencies: Review and update versions in Toolkit and Application modules
2. Security: Ensure SQLCipher usage and proper data encryption
3. Performance: Monitor APK size and runtime performance
4. Accessibility: Maintain compliance using automation test framework

## Important Notes

- Toolkit Dependency: The Toolkit is a git submodule; changes there affect multiple projects
- Environment Variables: Many configurations depend on `.env` file setup
- Signing: Release builds require proper keystore configuration
- Testing: Always run full test suite before significant changes
- Database: All data is encrypted; backup/restore requires proper key management

This guide provides Qwen Code with the essential knowledge to effectively work with the ShareConnect codebase while maintaining its architectural integrity and development standards.