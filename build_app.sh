#!/bin/bash

# ShareConnect Build Script
# This script helps build the ShareConnect Android application

echo "ShareConnect Build Script"
echo "========================="
echo ""

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "Error: build.gradle not found. Please run this script from the project root directory."
    exit 1
fi

echo "Cleaning previous build artifacts..."
gradle clean

if [ $? -ne 0 ]; then
    echo "Error: Clean failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "Building debug APK..."
gradle assembleDebug

if [ $? -ne 0 ]; then
    echo "Error: Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "Build successful!"
echo "APK location: app/build/outputs/apk/debug/app-debug.apk"

# Check if APK exists
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "APK size: $(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)"
else
    echo "Warning: APK file not found at expected location."
fi

echo ""
echo "To install the APK on a connected device, run:"
echo "adb install app/build/outputs/apk/debug/app-debug.apk"