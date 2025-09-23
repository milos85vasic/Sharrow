package com.shareconnect.suites

import com.shareconnect.activities.MainActivityInstrumentationTest
import com.shareconnect.activities.SettingsActivityInstrumentationTest
import com.shareconnect.database.HistoryRepositoryInstrumentationTest
import com.shareconnect.database.ThemeRepositoryInstrumentationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Instrumentation Test Suite for ShareConnect Application
 *
 * This suite runs all instrumentation tests that require Android context
 * and run on actual device or emulator.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ThemeRepositoryInstrumentationTest::class,
    HistoryRepositoryInstrumentationTest::class,
    MainActivityInstrumentationTest::class,
    SettingsActivityInstrumentationTest::class
)
class InstrumentationTestSuite