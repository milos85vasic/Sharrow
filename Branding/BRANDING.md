# Sharrow Branding Documentation

## Logo Design

The Sharrow logo combines the concepts of "share" and "arrow" into a single, memorable icon. The design represents the core function of the application - sharing content through a directional arrow metaphor.

### Color Palette

- **Warm Orange (#FF9800)**: Represents energy, enthusiasm, and the act of sharing
- **Carmine Red (#C62828)**: Symbolizes action, urgency, and importance
- **Blue (#2196F3)**: Represents technology, trust, and connectivity
- **White (#FFFFFF)**: Provides contrast and clarity

### Logo Variations

1. **Full Logo**: Contains the arrow icon with abstract share elements
2. **Icon Only**: Simplified arrow icon for app icons and small spaces
3. **Monochrome**: Single-color version for use on different backgrounds

## Design Principles

- **Simplicity**: Clean, recognizable shapes that work at any size
- **Scalability**: Maintains clarity from app icons to large displays
- **Versatility**: Works on both light and dark backgrounds
- **Meaningful**: Clearly represents the sharing functionality

## Asset Organization

```
branding/
├── assets/
│   ├── sharrow_logo.svg          # Full logo in SVG format
│   ├── sharrow_icon.svg          # App icon in SVG format
│   ├── sharrow_icon_mono.svg     # Monochrome version
│   └── png/                      # Generated PNG assets
│       ├── mdpi/
│       ├── hdpi/
│       ├── xhdpi/
│       ├── xxhdpi/
│       └── xxxhdpi/
├── BRANDING_GUIDE.md             # This document
└── generate_icons.sh             # Icon generation script
```

## Android Icon Assets

The app icon is provided in multiple resolutions to support all Android devices:

- **mdpi**: 48x48 px
- **hdpi**: 72x72 px
- **xhdpi**: 96x96 px
- **xxhdpi**: 144x144 px
- **xxxhdpi**: 192x192 px

### Adaptive Icons

Android 8.0+ supports adaptive icons with separate background and foreground layers:

- **Background**: Solid orange circle (#FF9800)
- **Foreground**: White arrow icon with orange and red elements

## Usage Guidelines

- Always maintain adequate clear space around the logo
- Do not distort or alter the logo proportions
- Use the monochrome version when the full color logo is not suitable
- Ensure proper contrast when placing on different backgrounds

## File Formats

- SVG (Scalable Vector Graphics) - Primary format
- PNG (Portable Network Graphics) - Raster format for web
- JPEG (Joint Photographic Experts Group) - For print materials

## Splash Screen

The splash screen features the Sharrow logo with the app name, using appropriate colors for both light and dark themes:

- **Light Theme**: Orange background (#FF9800)
- **Dark Theme**: Darker orange background (#F57C00)

## Brand Colors

The brand uses a consistent color palette throughout the application:

- Primary: Warm Orange (#FF9800)
- Secondary: Carmine Red (#C62828)
- Accent: Blue (#2196F3)
- Neutral: White (#FFFFFF) and Black (#000000)

These colors are defined in the app's `colors.xml` file and used consistently across all themes.