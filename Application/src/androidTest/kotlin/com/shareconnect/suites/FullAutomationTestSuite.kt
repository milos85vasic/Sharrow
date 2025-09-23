package com.shareconnect.suites

import com.shareconnect.automation.AccessibilityAutomationTest
import com.shareconnect.automation.FullAppFlowAutomationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Full Automation Test Suite for ShareConnect Application
 *
 * This suite runs comprehensive end-to-end tests that cover
 * complete user workflows, accessibility, and stress testing.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    FullAppFlowAutomationTest::class,
    AccessibilityAutomationTest::class
)
class FullAutomationTestSuite