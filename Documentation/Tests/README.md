# ShareConnect Testing Framework

This directory contains comprehensive test reports for the ShareConnect Android application.

## Directory Structure

```
Documentation/Tests/
├── README.md (this file)
└── YYYYMMDD_HHMMSS_TEST_ROUND/
    ├── master_test_summary.txt
    ├── unit_tests/
    │   ├── test_summary.txt
    │   ├── unit_test_execution.log
    │   └── [HTML/XML test reports]
    ├── instrumentation_tests/
    │   ├── test_summary.txt
    │   ├── instrumentation_test_execution.log
    │   └── [HTML/XML test reports]
    └── automation_tests/
        ├── test_summary.txt
        ├── automation_test_execution.log
        ├── device_final_state.png
        └── [HTML/XML test reports with screenshots]
```

## Test Execution Scripts

The following scripts are available in the project root to execute tests:

### Individual Test Scripts

1. **`run_unit_tests.sh`** - Executes unit tests
   - Tests core business logic and data models
   - Runs without requiring Android device
   - Fast execution for development feedback

2. **`run_instrumentation_tests.sh`** - Executes instrumentation tests
   - Tests Android components requiring device context
   - Requires connected Android device or emulator
   - Tests database operations and UI components

3. **`run_automation_tests.sh`** - Executes full automation tests
   - Tests complete user workflows end-to-end
   - Includes accessibility compliance verification
   - Requires connected Android device or emulator

### Master Test Script

4. **`run_all_tests.sh`** - Executes all three test types sequentially
   - Comprehensive testing of the entire application
   - Generates consolidated reports
   - Recommended for release validation

## Test Types Coverage

### Unit Tests (com.shareconnect.suites.UnitTestSuite)
- **ServerProfileTest**: Server profile data model validation
- **ProfileManagerTest**: Profile management operations
- **ThemeTest**: Theme data model and utilities
- **HistoryItemTest**: History data model validation
- **DialogUtilsTest**: Dialog utility functions testing
- **ServiceApiClientTest**: API client functionality testing

### Instrumentation Tests (com.shareconnect.suites.InstrumentationTestSuite)
- **ThemeRepositoryInstrumentationTest**: Theme persistence operations
- **HistoryRepositoryInstrumentationTest**: History database operations
- **MainActivityInstrumentationTest**: Main activity lifecycle and UI
- **SettingsActivityInstrumentationTest**: Settings screen functionality

### Automation Tests (com.shareconnect.suites.FullAutomationTestSuite)
- **FullAppFlowAutomationTest**: Complete user workflow testing
  - First run experience validation
  - Theme change workflow testing
  - Profile management operations
  - History viewing functionality
  - Share intent handling
  - Complete navigation flows
  - Application stress testing

- **AccessibilityAutomationTest**: Accessibility compliance verification
  - Content labeling validation
  - Touch target size compliance
  - Text contrast verification
  - Keyboard navigation support
  - Screen reader compatibility
  - State description accuracy

## Usage Examples

### Run All Tests
```bash
./run_all_tests.sh
```

### Run Individual Test Types
```bash
# Unit tests only
./run_unit_tests.sh

# Instrumentation tests only
./run_instrumentation_tests.sh

# Automation tests only
./run_automation_tests.sh
```

## Requirements

### For Unit Tests
- No additional requirements (runs on development machine)

### For Instrumentation and Automation Tests
- Connected Android device or running emulator
- USB debugging enabled on device
- Application debug permissions granted
- Device unlocked during test execution

## Report Interpretation

Each test execution generates:

1. **Execution Log**: Complete console output during test run
2. **Test Summary**: High-level results and metrics
3. **HTML Reports**: Detailed test results with pass/fail status
4. **XML Results**: JUnit-compatible results for CI/CD integration
5. **Screenshots**: (Automation tests) Visual verification of UI states

## Continuous Integration

The test scripts are designed to work with CI/CD pipelines:

- Exit codes indicate success (0) or failure (non-zero)
- JUnit XML format reports for integration with CI tools
- Comprehensive logging for debugging failures
- Timestamped reports prevent conflicts in parallel builds

## Troubleshooting

### Common Issues

1. **No devices found**: Ensure Android device/emulator is connected and USB debugging is enabled
2. **Tests fail to start**: Verify application builds successfully with `./gradlew assembleDebug`
3. **Permission errors**: Ensure scripts are executable with `chmod +x *.sh`
4. **OutOfMemory errors**: Increase Gradle JVM heap size in `gradle.properties`

### Debug Commands

```bash
# Check connected devices
adb devices -l

# Check application installation
adb shell pm list packages | grep shareconnect

# View device logs during tests
adb logcat | grep -i shareconnect
```

## Test Maintenance

- Update test cases when adding new features
- Maintain test data and mock objects
- Review accessibility compliance as UI changes
- Update expected values when business logic changes
- Keep device compatibility matrix updated

---

**Generated by ShareConnect Testing Framework**
For issues or questions, refer to the development team.