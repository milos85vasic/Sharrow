# MeTube Share

An Android application that allows you to share media links from various streaming services directly to your local MeTube instance.

## Features

- Share media links from YouTube, Vimeo, Twitch, Reddit, Twitter, Instagram, Facebook, SoundCloud and more
- Support for multiple MeTube server profiles
- Set a default server for quick sharing
- Modern Material Design UI
- Connection testing for server profiles
- Automatic redirection to browser after sending link
- Supports all services compatible with yt-dlp

## Supported Services

MeTube Share works with all streaming services supported by MeTube, which uses yt-dlp as its backend. This includes:

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

## Usage

1. Open any supported media app or website
2. Find a video or audio content you want to download
3. Tap the share button
4. Select "MeTube Share" from the sharing options
5. Choose your server profile (if you have multiple)
6. Tap "Send to MeTube"
7. The app will automatically open your MeTube instance in the browser

## Requirements

- Android 8.0 (API level 26) or higher
- A running MeTube instance accessible from your device

## Building

To build the application:

```bash
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

## Contributing

Feel free to fork this project and submit pull requests for improvements or bug fixes.

## License

This project is licensed under the MIT License - see the LICENSE file for details.