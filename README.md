# MeTube Share

An Android application that allows you to share YouTube links directly to your local MeTube instance.

## Features

- Share YouTube links from any app directly to your MeTube server
- Support for multiple MeTube server profiles
- Set a default server for quick sharing
- Modern Material Design UI
- Connection testing for server profiles
- Automatic redirection to browser after sending link

## Setup

1. Install the app on your Android device
2. Open the app and configure your MeTube server profiles:
   - Go to Settings > Server Profiles
   - Add a new profile with your server's URL and port
   - Set one profile as default for quick sharing
3. Test your connection to ensure the server is reachable

## Usage

1. Open any YouTube video in the YouTube app or website
2. Tap the share button
3. Select "MeTube Share" from the sharing options
4. Choose your server profile (if you have multiple)
5. Tap "Send to MeTube"
6. The app will automatically open your MeTube instance in the browser

## Requirements

- Android 7.0 (API level 24) or higher
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