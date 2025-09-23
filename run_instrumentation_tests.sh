#!/bin/bash

# ShareConnect - Instrumentation Tests Execution Script
# This script runs all instrumentation tests and generates reports

set -e

# Set Android SDK paths
export ANDROID_HOME="/Volumes/T7/Android/SDK"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get current timestamp for directory naming
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_TYPE="instrumentation_tests"
REPORT_DIR="Documentation/Tests/${TIMESTAMP}_TEST_ROUND/${TEST_TYPE}"

echo -e "${BLUE}ShareConnect Instrumentation Tests Execution${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

# Check if device/emulator is connected
echo -e "${YELLOW}Checking for connected devices...${NC}"
adb devices -l

DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -c "device$" || true)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}No devices found. Attempting to start emulator...${NC}"

    # Check for available emulators
    AVAILABLE_EMULATORS=$(emulator -list-avds 2>/dev/null || true)

    if [ -z "$AVAILABLE_EMULATORS" ]; then
        echo -e "${YELLOW}No emulators found. Creating a new emulator...${NC}"

        # Check if Android SDK is available
        if ! command -v avdmanager &> /dev/null; then
            echo -e "${RED}✗ Android SDK tools not found! Please install Android SDK.${NC}"
            exit 1
        fi

        # List available system images
        echo -e "${BLUE}Available system images:${NC}"
        avdmanager list target

        # Create a basic emulator with API 30 (common target)
        AVD_NAME="ShareConnect_Test_Emulator"
        echo -e "${YELLOW}Creating emulator: $AVD_NAME${NC}"
        echo "no" | avdmanager create avd -n "$AVD_NAME" -k "system-images;android-30;google_apis;x86_64" --force || {
            echo -e "${RED}✗ Failed to create emulator. Please check your Android SDK installation.${NC}"
            exit 1
        }
    else
        # Get the first available emulator
        AVD_NAME=$(echo "$AVAILABLE_EMULATORS" | head -1)
        echo -e "${GREEN}Found emulator: $AVD_NAME${NC}"
    fi

    # Start the emulator
    echo -e "${YELLOW}Starting emulator: $AVD_NAME${NC}"
    emulator -avd "$AVD_NAME" -no-snapshot-save -wipe-data > /dev/null 2>&1 &
    EMULATOR_PID=$!

    # Wait for emulator to boot
    echo -e "${YELLOW}Waiting for emulator to boot...${NC}"
    timeout=300  # 5 minutes timeout
    counter=0

    while [ $counter -lt $timeout ]; do
        if adb shell getprop sys.boot_completed 2>/dev/null | grep -q "1"; then
            echo -e "${GREEN}✓ Emulator booted successfully!${NC}"
            break
        fi

        if [ $((counter % 10)) -eq 0 ]; then
            echo -e "${BLUE}Still waiting... (${counter}s/${timeout}s)${NC}"
        fi

        sleep 1
        counter=$((counter + 1))
    done

    if [ $counter -ge $timeout ]; then
        echo -e "${RED}✗ Emulator failed to boot within $timeout seconds!${NC}"
        kill $EMULATOR_PID 2>/dev/null || true
        exit 1
    fi

    # Wait a bit more for the emulator to be fully ready
    echo -e "${YELLOW}Waiting for emulator to be ready...${NC}"
    sleep 10

    # Check again for connected devices
    DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -c "device$" || true)
fi

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}✗ No Android devices/emulators available after startup attempt!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found $DEVICE_COUNT connected device(s)${NC}"
echo ""

# Create report directory
mkdir -p "$REPORT_DIR"

echo -e "${YELLOW}Starting Instrumentation Tests...${NC}"
echo "Report will be saved to: $REPORT_DIR"
echo ""

# Build the app first
echo -e "${BLUE}Building application...${NC}"
./gradlew :Application:assembleDebug :Application:assembleDebugAndroidTest

# Run instrumentation tests with detailed output
echo -e "${BLUE}Running Instrumentation Test Suite...${NC}"
./gradlew :Application:connectedAndroidTest \
    --info \
    --stacktrace \
    2>&1 | tee "${REPORT_DIR}/instrumentation_test_execution.log"

# Check if tests passed
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo -e "${GREEN}✓ Instrumentation tests completed successfully!${NC}"
    TEST_STATUS="PASSED"
else
    echo -e "${RED}✗ Instrumentation tests failed!${NC}"
    TEST_STATUS="FAILED"
fi

# Copy test reports
echo -e "${BLUE}Copying test reports...${NC}"
if [ -d "Application/build/reports/androidTests/connected" ]; then
    cp -r Application/build/reports/androidTests/connected/* "${REPORT_DIR}/"
    echo -e "${GREEN}✓ HTML test reports copied${NC}"
fi

if [ -d "Application/build/outputs/androidTest-results/connected" ]; then
    cp -r Application/build/outputs/androidTest-results/connected/* "${REPORT_DIR}/"
    echo -e "${GREEN}✓ XML test results copied${NC}"
fi

# Copy any screenshots or additional artifacts
if [ -d "Application/build/outputs/connected_android_test_additional_output" ]; then
    cp -r Application/build/outputs/connected_android_test_additional_output/* "${REPORT_DIR}/"
    echo -e "${GREEN}✓ Additional test artifacts copied${NC}"
fi

# Generate summary report
cat > "${REPORT_DIR}/test_summary.txt" << EOF
ShareConnect Instrumentation Tests Execution Summary
===================================================

Execution Date: $(date)
Test Type: Instrumentation Tests
Test Suite: com.shareconnect.suites.InstrumentationTestSuite
Status: ${TEST_STATUS}
Device Information: $(adb devices | grep -v "List of devices" | head -1)

Test Classes Executed:
- ThemeRepositoryInstrumentationTest
- HistoryRepositoryInstrumentationTest
- MainActivityInstrumentationTest
- SettingsActivityInstrumentationTest

Report Location: ${REPORT_DIR}

Files Generated:
- instrumentation_test_execution.log: Full execution log
- test_summary.txt: This summary file
- index.html: HTML test report (if available)
- TEST-*.xml: JUnit XML results (if available)

Command Used:
./gradlew connectedAndroidTest --tests "com.shareconnect.suites.InstrumentationTestSuite" --info --stacktrace

Requirements:
- Connected Android device or emulator
- USB debugging enabled
- Application debug build

EOF

echo ""
echo -e "${BLUE}Instrumentation Tests Execution Complete${NC}"
echo -e "${BLUE}=======================================${NC}"
echo "Status: ${TEST_STATUS}"
echo "Report Directory: ${REPORT_DIR}"
echo ""

if [ "$TEST_STATUS" = "PASSED" ]; then
    echo -e "${GREEN}All instrumentation tests passed successfully!${NC}"
    exit 0
else
    echo -e "${RED}Some instrumentation tests failed. Check the reports for details.${NC}"
    exit 1
fi