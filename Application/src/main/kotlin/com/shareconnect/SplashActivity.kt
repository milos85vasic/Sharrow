package com.shareconnect

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme
        val themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)

        // Set appropriate splash layout based on theme
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            setContentView(R.layout.activity_splash_dark)
        } else {
            setContentView(R.layout.activity_splash)
        }

        // Wait for 2 seconds then start main activity
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            // Clear the task to ensure clean navigation
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }, 2000)
    }
}