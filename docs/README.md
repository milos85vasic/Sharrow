# Sharrow

An Android application that allows you to share media links from various streaming services directly to your local MeTube instance.

Sharrow combines the words "share" and "arrow" to represent the core functionality of the app - sharing content through a directional arrow metaphor.

## Features

- **Multi-Service Support**: Share media links from YouTube, Vimeo, Twitch, Reddit, Twitter, Instagram, Facebook, SoundCloud and more
- **Multiple Server Profiles**: Support for multiple MeTube server profiles with default profile selection
- **Customizable Themes**: 6 color schemes with light/dark variants (Warm Orange, Crimson, Light Blue, Purple, Green, Material)
- **Encrypted Storage**: All data including history and profiles stored with SQLCipher encryption
- **Comprehensive History**: Detailed sharing history with filtering by service, type, and profile
- **Modern Material Design**: Beautiful UI following Material Design 3 guidelines
- **Connection Testing**: Built-in server connection testing
- **Splash Screen**: Professional branded splash screen with theme support
- **Quick Access**: Direct access to MeTube interface from multiple locations
- **Bulk Cleanup**: Flexible history cleanup options (individual items, by service, by type, or all)

## Supported Services

Sharrow works with all streaming services supported by MeTube, which uses yt-dlp as its backend. This includes:

- YouTube
- Vimeo
- Dailymotion
- Twitch
- Reddit
- Twitter/X
- Instagram
- Facebook
- SoundCloud
- Bandcamp
- And dozens of other sites supported by yt-dlp

## Setup

1. Install the app on your Android device
2. Open the app and configure your MeTube server profiles:
   - Go to Settings > Server Profiles
   - Add a new profile with your server's URL and port
   - Set one profile as default for quick sharing
3. Test your connection to ensure the server is reachable
4. (Optional) Customize the app theme in Settings > Theme

## Usage

### Sharing Content

1. Open any supported media app or website (YouTube, Vimeo, Twitch, etc.)
2. Find a video or audio content you want to download
3. Tap the share button
4. Select "Sharrow" from the sharing options
5. Choose your server profile (if you have multiple)
6. Tap "Send to MeTube"
7. The app will automatically open your MeTube instance in the browser

### Managing History

1. Access history through the main menu or toolbar
2. Filter by service provider or media type
3. Resend any item to any profile
4. Delete individual items or use bulk cleanup options

### Theme Customization

1. Go to Settings > Theme
2. Choose from 6 color schemes
3. Select light or dark variant
4. Theme is applied immediately and saved for future sessions

## Requirements

- Android 8.0 (API level 26) or higher
- A running MeTube instance accessible from your device

## Building

To build the application:

```bash
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

## Branding

Sharrow features a professionally designed logo combining the concepts of "share" and "arrow":

- **Primary Colors**: Warm Orange (#FF9800) and Carmine Red (#C62828)
- **Supporting Colors**: Blue (#2196F3) and White (#FFFFFF)
- **Adaptive Icons**: Properly scaled for all Android devices
- **Splash Screen**: Themed loading screen with logo

All branding assets are available in the `branding/` directory.

## Architecture

### Data Storage

- **Room Database**: Local data storage with SQLCipher encryption
- **History Items**: Complete tracking of shared links with metadata
- **Themes**: Persistent theme preferences
- **Profiles**: Server profile management

### Security

- **Encrypted Storage**: All sensitive data encrypted at rest
- **Secure Networking**: HTTPS support for server communications
- **Data Privacy**: No data leaves the device without user action

### UI Components

- **Material Design 3**: Modern UI following Google's latest guidelines
- **Adaptive Layouts**: Responsive design for all screen sizes
- **Theme Support**: Dynamic theme switching with day/night mode
- **Intuitive Navigation**: Clear navigation patterns and user flows

## Contributing

Feel free to fork this project and submit pull requests for improvements or bug fixes.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [MeTube](https://github.com/alexta69/metube) - Self-hosted YouTube downloader
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) - Media download engine
- [Android Jetpack](https://developer.android.com/jetpack) - Android development components
- [Material Design](https://m3.material.io/) - Design system by Google