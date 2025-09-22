# ShareConnect - Multi-Service Media Sharing Application

## Overview

ShareConnect is an Android application that allows you to share media links from various streaming services and download sources directly to your local services including MeTube, YT-DLP, torrent clients, and jDownloader.

The application combines the words "share" and "connect" to represent the core functionality - connecting content discovery with local download services.

## Features

### Multi-Service Support
ShareConnect supports four major types of download services:

1. **MeTube Instances**: Traditional YouTube downloader support
2. **YT-DLP Services**: Modern media download service support
3. **Torrent Clients**: qBittorrent, Transmission, uTorrent
4. **jDownloader Instances**: Advanced download manager support

### Universal Media Sharing
Share content from a vast array of streaming platforms directly to your local services:
- YouTube (videos, playlists, channels)
- Vimeo
- Twitch
- Reddit
- Twitter/X
- Instagram
- Facebook
- SoundCloud
- Dailymotion
- Bandcamp
- And any other service supported by yt-dlp

### Magnet Link Support
Directly add magnet links to torrent clients for immediate downloading.

### Multiple Service Profiles
Support for multiple service profiles with default profile selection:
- Create and manage profiles for each service type
- Set a default profile for quick sharing
- Test service connections before saving
- Edit or delete existing profiles

### Encrypted Data Storage
All data including history and profiles stored with SQLCipher encryption:
- Secure database implementation using Room
- No plaintext data stored on the device
- Protected against unauthorized access

### Comprehensive Sharing History
Detailed tracking of all shared links with extensive metadata:
- Service provider (YouTube, Vimeo, etc.)
- Media type (single video, playlist, channel)
- Target service type (MeTube, YT-DLP, Torrent, jDownloader)
- Timestamp of sharing
- Target profile information
- Send success/failure status
- Filtering capabilities by any metadata field

### Theme Customization System
Six distinct color schemes with light and dark variants:
1. **Warm Orange** - Energetic and vibrant
2. **Crimson** - Bold and dramatic
3. **Light Blue** - Calm and refreshing
4. **Purple** - Creative and elegant
5. **Green** - Natural and soothing
6. **Material** - Default Material Design theme

### Professional Branding
Custom-designed ShareConnect logo with adaptive icons:
- Distinctive icon for light and dark themes
- Splash screen with theme awareness
- Consistent branding throughout the application
- SVG source assets for scalability

### Clipboard URL Sharing
Share URLs directly from clipboard without using the share intent:
- Dedicated "Add" button on home screen
- URL validation to prevent invalid submissions
- Seamless integration with existing sharing workflows

### System App Integration
Share links directly to installed applications that support them:
- Automatic detection of compatible apps
- App icons for visual recognition
- Direct sharing without leaving ShareConnect

## Supported Services

### For MeTube and YT-DLP:
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

### For Torrent Clients:
- Magnet Links
- Torrent Files
- HTTP(S) links to torrent files

### For jDownloader:
- Direct download links
- File hosting services (MediaFire, RapidGator, etc.)
- One-click hosting services

## Setup

### Installation
1. Install the ShareConnect APK on your Android device
2. Open the app and configure your service profiles:
   - Go to Settings > Server Profiles
   - Add new profiles for each service you want to use
   - Select the appropriate service type (MeTube, YT-DLP, Torrent Client, jDownloader)
   - For Torrent Clients, specify which client you're using
   - Set one profile as default for quick sharing
3. Test your connections to ensure services are reachable
4. (Optional) Customize the app theme in Settings > Theme

### Service Profile Configuration

#### MeTube Profile
- Service Type: MeTube
- URL: Your MeTube server address (e.g., http://192.168.1.100)
- Port: Your MeTube server port (typically 8081)

#### YT-DLP Profile
- Service Type: YT-DLP
- URL: Your YT-DLP server address (e.g., http://192.168.1.100)
- Port: Your YT-DLP server port (varies by setup)

#### Torrent Client Profile
- Service Type: Torrent Client
- Torrent Client: Select from qBittorrent, Transmission, or uTorrent
- URL: Your torrent client Web UI address (e.g., http://192.168.1.100)
- Port: Your torrent client Web UI port (varies by client)

#### jDownloader Profile
- Service Type: jDownloader
- URL: Your jDownloader address (e.g., http://192.168.1.100)
- Port: Your jDownloader port (typically varies by setup)

## Usage

### Sharing Content via Share Intent

1. Open any supported media app or website (YouTube, Vimeo, Twitch, etc.)
2. Find content you want to download
3. Tap the share button
4. Select "ShareConnect" from the sharing options
5. Choose your service profile (if you have multiple)
6. Tap "Send to Service"
7. The app will automatically open your service interface in the browser

### Sharing Content from Clipboard

1. Copy any supported URL to clipboard
2. Open ShareConnect app
3. Tap the "Add" button (floating action button)
4. The URL from clipboard will be automatically detected
5. Choose your service profile
6. Tap "Send to Service"
7. The app will automatically open your service interface in the browser

### Sharing Content to System Apps

1. Open any supported media app or website
2. Find content you want to download
3. Tap the share button
4. Select "ShareConnect" from the sharing options
5. Instead of selecting a ShareConnect profile, tap "Share to Apps"
6. Choose from the list of compatible installed applications
7. The link will be sent directly to the selected application

## Best Practices

### Security Considerations
1. Use HTTPS when possible for all services
2. Configure strong authentication for your services
3. Limit external access to service ports when not needed
4. Regularly update your services to the latest versions

### Network Configuration
1. Ensure all services are accessible from your device
2. Configure port forwarding if accessing services remotely
3. Use consistent naming for your profiles
4. Set a default profile for your most commonly used service

## Troubleshooting

Common issues and solutions:

1. **Connection Failed**: 
   - Verify URL and port are correct
   - Check that the service is running
   - Ensure network connectivity to the service

2. **Authentication Errors**:
   - Configure proper authentication for your services
   - Check credentials if required

3. **Content Not Recognized**:
   - Verify the URL is supported by the target service
   - Check service logs for detailed error information

## Technical Implementation

### Architecture
- **Room Database**: Local data storage with SQLCipher encryption
- **Repository Pattern**: Clean architecture with separation of concerns
- **MVVM**: Model-View-ViewModel pattern for UI components
- **Material Design 3**: Modern UI following Google's latest guidelines

### Security
- **Encrypted Storage**: All sensitive data encrypted at rest
- **Secure Networking**: HTTPS support for service communications
- **Input Validation**: Comprehensive validation for all user inputs
- **Data Privacy**: No data leaves the device without user action

### Supported Android Versions
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34 (Android 14)
- Compatible with all modern Android devices

## Development

### Building the Application
To build the application:

```bash
gradle assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

### Dependencies
- AndroidX libraries for modern Android development
- Room database for local storage
- SQLCipher for encryption
- Material Design 3 components
- OkHttp3 for network requests
- Gson for JSON serialization

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/shareconnect/
│   │   │   ├── database/          # Room database and DAOs
│   │   │   ├── ui/                # Activities and UI components
│   │   │   └── utils/            # Utility classes
│   │   └── res/                   # Resources (layouts, drawables, etc.)
│   └── test/                      # Unit tests
└── build.gradle                  # Module-level build configuration
```

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