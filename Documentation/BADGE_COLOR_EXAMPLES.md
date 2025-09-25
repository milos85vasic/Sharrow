# ShareConnect Badge Color Examples

This document shows how the README badges change color based on test success percentages.

## Color Coding System

The badges use the following color scheme based on test success percentage:

### 95-100% Success - Bright Green
```
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-100%25-brightgreen?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-98%25-brightgreen?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
```

### 80-94% Success - Green
```
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-85%25-green?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-92%25-green?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
```

### 60-79% Success - Yellow
```
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-75%25-yellow?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-68%25-yellow?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
```

### 40-59% Success - Orange
```
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-55%25-orange?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-42%25-orange?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
```

### 0-39% Success - Red
```
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-25%25-red?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-10%25-red?style=flat-square&logo=android)](Documentation/Tests/latest/unit_test_report.html)
```

## Badge Types

### Test Badges
- **Unit Tests**: Business logic and data model testing
- **Instrumentation Tests**: Android component integration testing
- **Automation Tests**: End-to-end user workflow testing

### Build Badge
- **Build Status**: Shows "Passing" (green) or "Failing" (red)

## Automatic Updates

The badges are automatically updated when running:
- `./run_all_tests.sh` - Runs all tests and updates badges
- `./update_badges.sh` - Updates badges based on latest test reports

## Link Behavior

Each badge links to the corresponding HTML test report in:
```
Documentation/Tests/latest/
├── unit_test_report.html
├── instrumentation_test_report.html
├── automation_test_report.html
└── build_report.html
```

The `latest` directory is a symlink to the most recent test execution round.