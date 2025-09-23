#!/bin/bash

# ShareConnect - Master Test Execution Script
# This script runs all three types of tests: Unit, Instrumentation, and Automation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Get current timestamp for directory naming
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
MASTER_REPORT_DIR="Documentation/Tests/${TIMESTAMP}_TEST_ROUND"

echo -e "${BOLD}${CYAN}ShareConnect Complete Test Suite Execution${NC}"
echo -e "${BOLD}${CYAN}==========================================${NC}"
echo ""
echo -e "${BLUE}Starting comprehensive testing of ShareConnect application${NC}"
echo -e "${BLUE}Test Round: ${TIMESTAMP}${NC}"
echo ""

# Create master report directory
mkdir -p "$MASTER_REPORT_DIR"

# Track test results
UNIT_TEST_STATUS="NOT_RUN"
INSTRUMENTATION_TEST_STATUS="NOT_RUN"
AUTOMATION_TEST_STATUS="NOT_RUN"

# Track execution times
START_TIME=$(date +%s)

echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}${YELLOW}                    PHASE 1: UNIT TESTS                    ${NC}"
echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Run Unit Tests
echo -e "${BLUE}Executing unit tests...${NC}"
if ./run_unit_tests.sh; then
    UNIT_TEST_STATUS="PASSED"
    echo -e "${GREEN}✓ Unit tests completed successfully${NC}"
else
    UNIT_TEST_STATUS="FAILED"
    echo -e "${RED}✗ Unit tests failed${NC}"
fi

UNIT_END_TIME=$(date +%s)
UNIT_DURATION=$((UNIT_END_TIME - START_TIME))

echo ""
echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}${YELLOW}                PHASE 2: INSTRUMENTATION TESTS            ${NC}"
echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Run Instrumentation Tests
echo -e "${BLUE}Executing instrumentation tests...${NC}"
if ./run_instrumentation_tests.sh; then
    INSTRUMENTATION_TEST_STATUS="PASSED"
    echo -e "${GREEN}✓ Instrumentation tests completed successfully${NC}"
else
    INSTRUMENTATION_TEST_STATUS="FAILED"
    echo -e "${RED}✗ Instrumentation tests failed${NC}"
fi

INSTRUMENTATION_END_TIME=$(date +%s)
INSTRUMENTATION_DURATION=$((INSTRUMENTATION_END_TIME - UNIT_END_TIME))

echo ""
echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}${YELLOW}                PHASE 3: AUTOMATION TESTS                 ${NC}"
echo -e "${BOLD}${YELLOW}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Run Automation Tests
echo -e "${BLUE}Executing full automation tests...${NC}"
if ./run_automation_tests.sh; then
    AUTOMATION_TEST_STATUS="PASSED"
    echo -e "${GREEN}✓ Automation tests completed successfully${NC}"
else
    AUTOMATION_TEST_STATUS="FAILED"
    echo -e "${RED}✗ Automation tests failed${NC}"
fi

AUTOMATION_END_TIME=$(date +%s)
AUTOMATION_DURATION=$((AUTOMATION_END_TIME - INSTRUMENTATION_END_TIME))
TOTAL_DURATION=$((AUTOMATION_END_TIME - START_TIME))

# Determine overall status
OVERALL_STATUS="PASSED"
if [ "$UNIT_TEST_STATUS" != "PASSED" ] || [ "$INSTRUMENTATION_TEST_STATUS" != "PASSED" ] || [ "$AUTOMATION_TEST_STATUS" != "PASSED" ]; then
    OVERALL_STATUS="FAILED"
fi

echo ""
echo -e "${BOLD}${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}${CYAN}                    EXECUTION SUMMARY                      ${NC}"
echo -e "${BOLD}${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Generate master summary report
cat > "${MASTER_REPORT_DIR}/master_test_summary.txt" << EOF
ShareConnect Complete Test Suite Execution Summary
=================================================

Execution Date: $(date)
Test Round ID: ${TIMESTAMP}
Overall Status: ${OVERALL_STATUS}

═══════════════════════════════════════════════════════════

PHASE 1: UNIT TESTS
Status: ${UNIT_TEST_STATUS}
Duration: ${UNIT_DURATION} seconds
Test Suite: com.shareconnect.suites.UnitTestSuite
Coverage: Core business logic and data models

Test Classes:
- ServerProfileTest: Server profile data model testing
- ProfileManagerTest: Profile management operations
- ThemeTest: Theme data model and utilities
- HistoryItemTest: History data model validation
- DialogUtilsTest: Dialog utility functions
- ServiceApiClientTest: API client functionality

═══════════════════════════════════════════════════════════

PHASE 2: INSTRUMENTATION TESTS
Status: ${INSTRUMENTATION_TEST_STATUS}
Duration: ${INSTRUMENTATION_DURATION} seconds
Test Suite: com.shareconnect.suites.InstrumentationTestSuite
Coverage: Android components requiring device context

Test Classes:
- ThemeRepositoryInstrumentationTest: Theme persistence testing
- HistoryRepositoryInstrumentationTest: History database operations
- MainActivityInstrumentationTest: Main activity lifecycle and UI
- SettingsActivityInstrumentationTest: Settings screen functionality

═══════════════════════════════════════════════════════════

PHASE 3: AUTOMATION TESTS
Status: ${AUTOMATION_TEST_STATUS}
Duration: ${AUTOMATION_DURATION} seconds
Test Suite: com.shareconnect.suites.FullAutomationTestSuite
Coverage: Complete user workflows and accessibility

Test Classes:
- FullAppFlowAutomationTest: End-to-end user workflows
  * First run experience
  * Theme change workflows
  * Profile management operations
  * History viewing functionality
  * Share intent handling
  * Complete navigation flows
  * Application stress testing

- AccessibilityAutomationTest: Accessibility compliance
  * Content labeling verification
  * Touch target size validation
  * Text contrast compliance
  * Keyboard navigation support
  * Screen reader compatibility
  * State description accuracy

═══════════════════════════════════════════════════════════

EXECUTION METRICS
Total Duration: ${TOTAL_DURATION} seconds ($(printf '%02d:%02d:%02d' $((TOTAL_DURATION/3600)) $((TOTAL_DURATION%3600/60)) $((TOTAL_DURATION%60))))
Unit Tests: ${UNIT_DURATION}s
Instrumentation Tests: ${INSTRUMENTATION_DURATION}s
Automation Tests: ${AUTOMATION_DURATION}s

COVERAGE SUMMARY
✓ Business Logic: Unit tests verify core functionality
✓ Data Persistence: Instrumentation tests verify database operations
✓ User Interface: Instrumentation tests verify UI components
✓ User Workflows: Automation tests verify complete user journeys
✓ Accessibility: Automation tests verify compliance standards

REPORT STRUCTURE
${MASTER_REPORT_DIR}/
├── master_test_summary.txt (this file)
├── unit_tests/
│   ├── test_summary.txt
│   ├── unit_test_execution.log
│   └── [HTML/XML reports]
├── instrumentation_tests/
│   ├── test_summary.txt
│   ├── instrumentation_test_execution.log
│   └── [HTML/XML reports]
└── automation_tests/
    ├── test_summary.txt
    ├── automation_test_execution.log
    ├── device_final_state.png
    └── [HTML/XML reports with screenshots]

COMMAND EXECUTION
./run_all_tests.sh

This master script executed:
1. ./run_unit_tests.sh
2. ./run_instrumentation_tests.sh
3. ./run_automation_tests.sh

═══════════════════════════════════════════════════════════

EOF

# Display summary
echo -e "${BLUE}Test Execution Summary:${NC}"
echo -e "${BLUE}=======================${NC}"
echo ""
echo -e "Unit Tests:          ${UNIT_TEST_STATUS} (${UNIT_DURATION}s)"
echo -e "Instrumentation:     ${INSTRUMENTATION_TEST_STATUS} (${INSTRUMENTATION_DURATION}s)"
echo -e "Automation:          ${AUTOMATION_TEST_STATUS} (${AUTOMATION_DURATION}s)"
echo ""
echo -e "Total Duration:      ${TOTAL_DURATION}s ($(printf '%02d:%02d:%02d' $((TOTAL_DURATION/3600)) $((TOTAL_DURATION%3600/60)) $((TOTAL_DURATION%60))))"
echo -e "Overall Status:      ${OVERALL_STATUS}"
echo ""
echo -e "${BLUE}Report Directory:    ${MASTER_REPORT_DIR}${NC}"
echo ""

if [ "$OVERALL_STATUS" = "PASSED" ]; then
    echo -e "${BOLD}${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    echo -e "${GREEN}ShareConnect application has been thoroughly tested and verified.${NC}"
    echo -e "${GREEN}✓ Business logic is correct${NC}"
    echo -e "${GREEN}✓ Database operations work properly${NC}"
    echo -e "${GREEN}✓ User interface functions as expected${NC}"
    echo -e "${GREEN}✓ Complete user workflows are validated${NC}"
    echo -e "${GREEN}✓ Accessibility standards are met${NC}"
    exit 0
else
    echo -e "${BOLD}${RED}❌ SOME TESTS FAILED ❌${NC}"
    echo -e "${RED}Please review the individual test reports for details.${NC}"

    if [ "$UNIT_TEST_STATUS" != "PASSED" ]; then
        echo -e "${RED}• Unit tests need attention${NC}"
    fi
    if [ "$INSTRUMENTATION_TEST_STATUS" != "PASSED" ]; then
        echo -e "${RED}• Instrumentation tests need attention${NC}"
    fi
    if [ "$AUTOMATION_TEST_STATUS" != "PASSED" ]; then
        echo -e "${RED}• Automation tests need attention${NC}"
    fi

    exit 1
fi