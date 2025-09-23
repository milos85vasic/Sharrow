# ShareConnect Signing Configuration

## Overview
The app's build.gradle is configured to use environment variables or a `.env.properties` file for signing configuration. This keeps sensitive information out of version control.

## Configuration Methods

### Method 1: Environment Variables
Export the following environment variables in your shell:

```bash
# Development signing
export SHARECONNECT_DEV_KEY_ALIAS="your_key_alias"
export SHARECONNECT_DEV_KEY_PASSWORD="your_key_password"
export SHARECONNECT_DEV_STORE_PASSWORD="your_store_password"
export SHARECONNECT_DEV_KEYSTORE_PATH="Application/Signing/dev.jks"

# Production/Cloud signing
export SHARECONNECT_CLOUD_KEY_ALIAS="your_key_alias"
export SHARECONNECT_CLOUD_KEY_PASSWORD="your_key_password"
export SHARECONNECT_CLOUD_STORE_PASSWORD="your_store_password"
export SHARECONNECT_CLOUD_KEYSTORE_PATH="Application/Signing/cloud.jks"
```

### Method 2: .env.properties File
1. Copy `.env.properties.example` to `.env.properties` in the project root
2. Fill in your actual values in the `.env.properties` file
3. The file is already in .gitignore and will not be committed

## Current Keystore Status
- Development keystore exists at: `Application/Signing/dev.jks`
- You need to either:
  1. Get the passwords for the existing keystore from the project owner
  2. OR create a new keystore (see below)

## Creating a New Keystore
If you need to create a new keystore for development:

```bash
keytool -genkey -v -keystore Application/Signing/dev_new.jks \
  -alias your_alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your_store_password \
  -keypass your_key_password
```

Then update your environment variables or `.env.properties` file with the new keystore path and credentials.

## Build Without Signing
If you just want to build for testing and don't need signing:
- The build will use Android's default debug keystore if no custom keystore is configured
- Simply ensure the keystore file doesn't exist or rename it temporarily

## Troubleshooting
- If you get "keystore password was incorrect" error: You need the correct passwords for the existing keystore
- If you get "keystore not found" error: Check the path in your environment variables
- The build.gradle checks if keystores exist before trying to use them