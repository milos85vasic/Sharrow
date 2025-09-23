package com.shareconnect

import com.redelf.commons.application.BaseApplication

class SCApplication : BaseApplication() {

    override fun firebaseEnabled() = isProduction()
    override fun firebaseAnalyticsEnabled() = isProduction()

    override fun isProduction(): Boolean {

        return resources.getBoolean(R.bool.is_production)
    }

    override fun takeSalt(): String {

        return getString(R.string.app_name)
    }
}