#!/bin/bash

# ShareConnect - Unit Tests Execution Script
# This script runs all unit tests and generates reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get current timestamp for directory naming
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_TYPE="unit_tests"
REPORT_DIR="Documentation/Tests/${TIMESTAMP}_TEST_ROUND/${TEST_TYPE}"

echo -e "${BLUE}ShareConnect Unit Tests Execution${NC}"
echo -e "${BLUE}=================================${NC}"
echo ""

# Create report directory
mkdir -p "$REPORT_DIR"

echo -e "${YELLOW}Starting Unit Tests...${NC}"
echo "Report will be saved to: $REPORT_DIR"
echo ""

# Run unit tests with detailed output
echo -e "${BLUE}Running Unit Test Suite...${NC}"
./gradlew :Application:test \
    --continue \
    2>&1 | tee "${REPORT_DIR}/unit_test_execution.log"

# Check if tests passed
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo -e "${GREEN}✓ Unit tests completed successfully!${NC}"
    TEST_STATUS="PASSED"
else
    echo -e "${RED}✗ Unit tests failed!${NC}"
    TEST_STATUS="FAILED"
fi

# Copy test reports
echo -e "${BLUE}Copying test reports...${NC}"
if [ -d "Application/build/reports/tests/testDebugUnitTest" ]; then
    cp -r Application/build/reports/tests/testDebugUnitTest/* "${REPORT_DIR}/"
    echo -e "${GREEN}✓ HTML test reports copied${NC}"
fi

if [ -d "Application/build/test-results/testDebugUnitTest" ]; then
    cp -r Application/build/test-results/testDebugUnitTest/* "${REPORT_DIR}/"
    echo -e "${GREEN}✓ XML test results copied${NC}"
fi
cat > "${REPORT_DIR}/test_summary.txt" << EOF
ShareConnect Unit Tests Execution Summary
=========================================

Execution Date: $(date)
Test Type: Unit Tests
Test Suite: com.shareconnect.suites.UnitTestSuite
Status: ${TEST_STATUS}

Test Classes Executed:
- ServerProfileTest
- ProfileManagerTest
- ThemeTest
- HistoryItemTest
- DialogUtilsTest
- ServiceApiClientTest

Report Location: ${REPORT_DIR}

Files Generated:
- unit_test_execution.log: Full execution log
- test_summary.txt: This summary file
- index.html: HTML test report (if available)
- TEST-*.xml: JUnit XML results (if available)

Command Used:
./gradlew test --tests "com.shareconnect.suites.UnitTestSuite" --info --stacktrace

EOF

echo ""
echo -e "${BLUE}Unit Tests Execution Complete${NC}"
echo -e "${BLUE}=============================${NC}"
echo "Status: ${TEST_STATUS}"
echo "Report Directory: ${REPORT_DIR}"
echo ""

if [ "$TEST_STATUS" = "PASSED" ]; then
    echo -e "${GREEN}All unit tests passed successfully!${NC}"
    exit 0
else
    echo -e "${RED}Some unit tests failed. Check the reports for details.${NC}"
    exit 1
fi