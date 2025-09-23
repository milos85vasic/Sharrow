package com.shareconnect.suites

import com.shareconnect.api.ServiceApiClientTest
import com.shareconnect.database.HistoryItemTest
import com.shareconnect.database.ThemeTest
import com.shareconnect.manager.ProfileManagerTest
import com.shareconnect.model.ServerProfileTest
import com.shareconnect.utils.DialogUtilsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Unit Test Suite for ShareConnect Application
 *
 * This suite runs all unit tests that don't require Android context
 * or can run with Robolectric.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ServerProfileTest::class,
    ProfileManagerTest::class,
    ThemeTest::class,
    HistoryItemTest::class,
    DialogUtilsTest::class,
    ServiceApiClientTest::class
)
class UnitTestSuite