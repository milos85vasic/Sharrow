package com.shareconnect.suites

import com.shareconnect.automation.AccessibilityAutomationTest
import com.shareconnect.automation.FullAppFlowAutomationTest
import com.shareconnect.automation.WebUIAutomationTest
import com.shareconnect.automation.ProfileFilteringAutomationTest
import com.shareconnect.automation.ShareActivityLayoutAndFlowTest
import com.shareconnect.automation.SystemAppDetectionAutomationTest
import com.shareconnect.automation.QBittorrentCompleteFlowAutomationTest
import com.shareconnect.automation.SmartProfileSelectionAutomationTest
import com.shareconnect.automation.MagnetMetadataAutomationTest
import com.shareconnect.automation.ProfilesListScrollingAutomationTest
import com.shareconnect.automation.WebUITorrentApplicationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Full Automation Test Suite for ShareConnect Application
 *
 * This suite runs comprehensive end-to-end tests that cover
 * complete user workflows, accessibility, stress testing, WebUI functionality,
 * intelligent profile filtering, magnet link metadata extraction, profiles list UI,
 * and WebUI torrent application workflows.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    FullAppFlowAutomationTest::class,
    AccessibilityAutomationTest::class,
    WebUIAutomationTest::class,
    ProfileFilteringAutomationTest::class,
    ShareActivityLayoutAndFlowTest::class,
    SystemAppDetectionAutomationTest::class,
    QBittorrentCompleteFlowAutomationTest::class,
    SmartProfileSelectionAutomationTest::class,
    MagnetMetadataAutomationTest::class,
    ProfilesListScrollingAutomationTest::class,
    WebUITorrentApplicationTest::class
)
class FullAutomationTestSuite