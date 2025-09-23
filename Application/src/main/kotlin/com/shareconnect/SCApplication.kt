package com.shareconnect

import com.redelf.commons.application.BaseApplication

class SCApplication : BaseApplication() {

    override fun isProduction(): Boolean {

        return false
    }

    override fun takeSalt(): String {

        return getString(R.string.app_name)
    }
}