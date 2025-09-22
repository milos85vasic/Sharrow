#!/bin/bash

# Create Android mipmap directories
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi

# Create adaptive icon directories
mkdir -p app/src/main/res/mipmap-anydpi-v26

echo "Android mipmap directories created successfully!"