#!/bin/bash

# ShareConnect - Instrumentation Tests Execution Script
# This script runs all instrumentation tests and generates reports

set -e

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
    echo -e "${RED}✗ No Android devices/emulators found!${NC}"
    echo "Please connect a device or start an emulator before running instrumentation tests."
    exit 1
fi

echo -e "${GREEN}✓ Found $DEVICE_COUNT connected device(s)${NC}"
echo ""

# Create report directory
mkdir -p "$REPORT_DIR"

# Change to Application directory
cd Application

echo -e "${YELLOW}Starting Instrumentation Tests...${NC}"
echo "Report will be saved to: $REPORT_DIR"
echo ""

# Build the app first
echo -e "${BLUE}Building application...${NC}"
./gradlew assembleDebug assembleDebugAndroidTest

# Run instrumentation tests with detailed output
echo -e "${BLUE}Running Instrumentation Test Suite...${NC}"
./gradlew connectedAndroidTest \
    --tests "com.shareconnect.suites.InstrumentationTestSuite" \
    --info \
    --stacktrace \
    2>&1 | tee "../${REPORT_DIR}/instrumentation_test_execution.log"

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
if [ -d "build/reports/androidTests/connected" ]; then
    cp -r build/reports/androidTests/connected/* "../${REPORT_DIR}/"
    echo -e "${GREEN}✓ HTML test reports copied${NC}"
fi

if [ -d "build/outputs/androidTest-results/connected" ]; then
    cp -r build/outputs/androidTest-results/connected/* "../${REPORT_DIR}/"
    echo -e "${GREEN}✓ XML test results copied${NC}"
fi

# Copy any screenshots or additional artifacts
if [ -d "build/outputs/connected_android_test_additional_output" ]; then
    cp -r build/outputs/connected_android_test_additional_output/* "../${REPORT_DIR}/"
    echo -e "${GREEN}✓ Additional test artifacts copied${NC}"
fi

# Generate summary report
cd ..
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