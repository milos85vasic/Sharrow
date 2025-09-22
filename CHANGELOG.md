# Changelog

All notable changes to Sharrow will be documented in this file.

## [1.0.0] - 2025-09-22

### Added
- Initial release of Sharrow (formerly MeTube Share)
- Multi-service support for YouTube, Vimeo, Twitch, Reddit, Twitter, Instagram, Facebook, SoundCloud, and more
- Multiple server profile management with default profile selection
- Encrypted local storage using Room database with SQLCipher
- Comprehensive sharing history with detailed metadata tracking
- Theme customization system with 6 color schemes and light/dark variants
- Professional branding with Sharrow logo and adaptive icons
- Splash screen with theme support
- Connection testing for server profiles
- Quick access to MeTube interface from multiple locations
- Bulk history cleanup options (individual items, by service, by type, or all)

### Changed
- Renamed application from "MeTube Share" to "Sharrow"
- Updated all UI references to use new branding
- Enhanced Material Design 3 implementation
- Improved user experience with better navigation and workflows
- Added detailed error handling and user feedback

### Removed
- Simplified UI elements that didn't add value to core functionality

## Features by Category

### Core Functionality
- Share media links from 10+ streaming services to MeTube
- Multiple server profile support with default selection
- Automatic browser redirection after sending links

### Data Management
- Encrypted storage for all application data
- Comprehensive sharing history with filtering capabilities
- Profile management with validation and testing

### User Interface
- Modern Material Design 3 implementation
- 6 customizable color themes with light/dark variants
- Professional branding with adaptive icons
- Splash screen with theme awareness
- Intuitive navigation and clear user flows

### Security
- SQLCipher encryption for all local data
- HTTPS support for server communications
- No data collection or transmission without user action

## Technical Implementation

### Architecture
- Room database with SQLCipher for encrypted storage
- Repository pattern for data management
- Singleton pattern for theme and profile managers
- Material Design 3 components throughout

### Dependencies
- AndroidX libraries for modern Android development
- Room database for local storage
- SQLCipher for encryption
- Material Design 3 components
- OkHttp3 for network requests

### Supported Android Versions
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34 (Android 14)
- Compatible with all modern Android devices