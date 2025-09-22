![ShareConnect](assets/Dark_Banner.jpg)

# ShareConnect

An Android application that allows you to share media links from various streaming services and download sources directly to your local services including MeTube, YT-DLP, torrent clients, and jDownloader.

ShareConnect combines the words "share" and "connect" to represent the core functionality of the app - connecting content discovery with local download services.

## Features

- **Multi-Service Support**: Share to MeTube, YT-DLP, qBittorrent, Transmission, uTorrent, and jDownloader
- **Universal Media Sharing**: Share content from YouTube, Vimeo, Twitch, Reddit, Twitter, Instagram, Facebook, SoundCloud and more
- **Magnet Link Support**: Directly add magnet links to torrent clients
- **Multiple Service Profiles**: Support for multiple service profiles with default profile selection
- **Clipboard URL Sharing**: Share URLs directly from clipboard
- **System App Integration**: Share links to compatible installed applications
- **Customizable Themes**: 6 color schemes with light/dark variants (Warm Orange, Crimson, Light Blue, Purple, Green, Material)
- **Encrypted Storage**: All data including history and profiles stored with SQLCipher encryption
- **Comprehensive History**: Detailed sharing history with filtering by service, type, and profile
- **Modern Material Design**: Beautiful UI following Material Design 3 guidelines
- **Connection Testing**: Built-in service connection testing
- **Quick Access**: Direct access to service interfaces from multiple locations
- **Bulk Cleanup**: Flexible history cleanup options (individual items, by service, by type, or all)

## Supported Services

ShareConnect works with all streaming services and download sources supported by your target services:

### For MeTube and YT-DLP

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

### For Torrent Clients

- Magnet Links
- Torrent Files
- HTTP(S) links to torrent files

### For jDownloader

- Direct download links
- File hosting services (MediaFire, RapidGator, etc.)
- One-click hosting services

## Setup

1. Install the app on your Android device
2. Open the app and configure your service profiles:
   - Go to Settings > Server Profiles
   - Add new profiles for each service you want to use
   - Select the appropriate service type (MeTube, YT-DLP, Torrent Client, jDownloader)
   - For Torrent Clients, specify which client you're using
   - Set one profile as default for quick sharing
3. Test your connections to ensure services are reachable
4. (Optional) Customize the app theme in Settings > Theme

## Usage

### Sharing Content

1. Open any supported media app or website (YouTube, Vimeo, Twitch, etc.)
2. Find content you want to download
3. Tap the share button
4. Select "ShareConnect" from the sharing options
5. Choose your service profile (if you have multiple)
6. Tap "Send to Service"
7. The app will automatically open your service interface in the browser

### Sharing from Clipboard

1. Copy any supported URL to clipboard
2. Open ShareConnect app
3. Tap the "Add" button (floating action button)
4. The URL from clipboard will be automatically detected
5. Choose your service profile
6. Tap "Send to Service"
7. The app will automatically open your service interface in the browser

### Sharing to System Apps

1. Open any supported media app or website
2. Find content you want to download
3. Tap the share button
4. Select "ShareConnect" from the sharing options
5. Instead of selecting a ShareConnect profile, tap "Share to Apps"
6. Choose from the list of compatible installed applications
7. The link will be sent directly to the selected application

### Managing History

1. Access history through the main menu or toolbar
2. Filter by service provider or media type
3. Resend any item to any profile or system app
4. Delete individual items or use bulk cleanup options

### Theme Customization

1. Go to Settings > Theme
2. Choose from 6 color schemes
3. Select light or dark variant
4. Theme is applied immediately and saved for future sessions

## Requirements

- Android 8.0 (API level 26) or higher
- Running service instances accessible from your device:
  - MeTube or YT-DLP (optional)
  - Torrent Client with Web UI (qBittorrent, Transmission, or uTorrent)
  - jDownloader with Web UI (optional)

## Building

To build the application:

```bash
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

## Branding

ShareConnect features a professionally designed logo combining the concepts of "share" and "connect":

- **Supporting Colors**: Blue (#2196F3) and White (#FFFFFF)
- **Adaptive Icons**: Properly scaled for all Android devices
- **Splash Screen**: Themed loading screen with logo

All branding assets are available in the `branding/` directory.

## Architecture

### Data Storage

- **Room Database**: Local data storage with SQLCipher encryption
- **History Items**: Complete tracking of shared links with metadata
- **Themes**: Persistent theme preferences
- **Profiles**: Service profile management

### Security

- **Encrypted Storage**: All sensitive data encrypted at rest
- **Secure Networking**: HTTPS support for service communications
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
- [qBittorrent](https://www.qbittorrent.org/) - Free BitTorrent client
- [Transmission](https://transmissionbt.com/) - Fast, easy, free BitTorrent client
- [uTorrent](https://www.utorrent.com/) - Proprietary BitTorrent client
- [jDownloader](https://jdownloader.org/) - Free download manager
- [Android Jetpack](https://developer.android.com/jetpack) - Android development components
- [Material Design](https://m3.material.io/) - Design system by Google
