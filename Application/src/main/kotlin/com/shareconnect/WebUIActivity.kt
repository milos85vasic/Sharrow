package com.shareconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.redelf.commons.logging.Console
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class WebUIActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var profile: ServerProfile? = null
    private var urlToShare: String? = null
    private var themeManager: ThemeManager? = null
    private var isAuthenticated = false
    private var authenticationAttempted = false

    companion object {
        private const val EXTRA_PROFILE = "profile"
        private const val EXTRA_URL_TO_SHARE = "url_to_share"

        fun startWebUI(context: Context, profile: ServerProfile, urlToShare: String? = null) {
            val intent = Intent(context, WebUIActivity::class.java)
            intent.putExtra(EXTRA_PROFILE, profile)
            urlToShare?.let { intent.putExtra(EXTRA_URL_TO_SHARE, it) }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_ui)

        // Get profile and URL from intent
        profile = intent.getParcelableExtra(EXTRA_PROFILE)
        urlToShare = intent.getStringExtra(EXTRA_URL_TO_SHARE)

        if (profile == null) {
            Toast.makeText(this, "Invalid profile", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        initViews()
        setupWebView()
        loadWebUI()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "${profile?.name} Web UI"
        }
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView?.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                // setAppCacheEnabled is deprecated in API 33+ and should be removed
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                loadWithOverviewMode = true
                useWideViewPort = true

                // Set user agent to avoid mobile redirects
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            }

            webViewClient = createWebViewClient()
            webChromeClient = createWebChromeClient()
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Allow navigation within the same domain
                val url = request?.url.toString()
                val baseUrl = "${profile?.url}:${profile?.port}"

                return if (url.startsWith(baseUrl)) {
                    false // Allow WebView to load
                } else {
                    // Open external URLs in default browser
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Console.error(e, "Failed to open external URL: $url")
                    }
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility = View.GONE

                // Attempt authentication and URL passing after page loads
                if (!authenticationAttempted) {
                    authenticationAttempted = true
                    handlePostPageLoad(url)
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                progressBar?.visibility = View.GONE

                val errorMessage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    error?.description?.toString() ?: "Unknown error"
                } else {
                    "Network error"
                }

                Toast.makeText(this@WebUIActivity, "Error loading page: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar?.progress = newProgress

                if (newProgress == 100) {
                    progressBar?.visibility = View.GONE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                supportActionBar?.subtitle = title
            }
        }
    }

    private fun loadWebUI() {
        val baseUrl = "${profile?.url}:${profile?.port}"
        webView?.loadUrl(baseUrl)
    }

    private fun handlePostPageLoad(url: String?) {
        when (profile?.serviceType) {
            ServerProfile.TYPE_TORRENT -> handleTorrentClientAuth(url)
            ServerProfile.TYPE_JDOWNLOADER -> handleJDownloaderAuth(url)
            // Add other service types as needed
            else -> {
                // For services without specific auth handling, just pass the URL
                passUrlToWebUI()
            }
        }
    }

    private fun handleTorrentClientAuth(url: String?) {
        when (profile?.torrentClientType) {
            ServerProfile.TORRENT_CLIENT_QBITTORRENT -> authenticateQBittorrent()
            ServerProfile.TORRENT_CLIENT_TRANSMISSION -> authenticateTransmission()
            ServerProfile.TORRENT_CLIENTUTORRENT -> authenticateUTorrent()
            else -> passUrlToWebUI()
        }
    }

    private fun authenticateQBittorrent() {
        if (profile?.username.isNullOrEmpty() || profile?.password.isNullOrEmpty()) {
            // No credentials provided, just pass the URL
            passUrlToWebUI()
            return
        }

        // JavaScript to auto-fill and submit login form
        val loginScript = """
            (function() {
                // Wait for login form to be available
                function waitForLogin() {
                    var usernameField = document.querySelector('input[name="username"], input[id="username"], input[type="text"]');
                    var passwordField = document.querySelector('input[name="password"], input[id="password"], input[type="password"]');
                    var loginButton = document.querySelector('input[type="submit"], button[type="submit"], button:contains("Login"), input[value*="Login"]');

                    if (usernameField && passwordField) {
                        usernameField.value = '${profile?.username}';
                        passwordField.value = '${profile?.password}';

                        if (loginButton) {
                            loginButton.click();
                        } else {
                            // Try to submit the form
                            var form = usernameField.closest('form');
                            if (form) {
                                form.submit();
                            }
                        }
                        return true;
                    }
                    return false;
                }

                if (!waitForLogin()) {
                    setTimeout(waitForLogin, 1000);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(loginScript) { result ->
            Console.log("qBittorrent authentication script executed: $result")
            isAuthenticated = true

            // Wait a bit for authentication to complete, then pass URL
            webView?.postDelayed({
                passUrlToWebUI()
            }, 3000)
        }
    }

    private fun authenticateTransmission() {
        // Transmission typically uses HTTP basic auth or session-based auth
        if (profile?.username.isNullOrEmpty() || profile?.password.isNullOrEmpty()) {
            passUrlToWebUI()
            return
        }

        // For Transmission, authentication is usually handled at the HTTP level
        // The WebView should prompt for credentials or handle it automatically
        isAuthenticated = true
        passUrlToWebUI()
    }

    private fun authenticateUTorrent() {
        if (profile?.username.isNullOrEmpty() || profile?.password.isNullOrEmpty()) {
            passUrlToWebUI()
            return
        }

        // uTorrent WebUI authentication handling
        val authScript = """
            (function() {
                // uTorrent WebUI specific authentication
                function authenticateUTorrent() {
                    var passwordField = document.querySelector('input[name="password"], input[type="password"]');
                    var loginButton = document.querySelector('input[type="submit"], button[type="submit"]');

                    if (passwordField) {
                        passwordField.value = '${profile?.password}';
                        if (loginButton) {
                            loginButton.click();
                        }
                        return true;
                    }
                    return false;
                }

                if (!authenticateUTorrent()) {
                    setTimeout(authenticateUTorrent, 1000);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(authScript) { result ->
            Console.log("uTorrent authentication script executed: $result")
            isAuthenticated = true

            webView?.postDelayed({
                passUrlToWebUI()
            }, 2000)
        }
    }

    private fun handleJDownloaderAuth(url: String?) {
        // JDownloader MyJDownloader authentication would go here
        passUrlToWebUI()
    }

    private fun passUrlToWebUI() {
        if (urlToShare.isNullOrEmpty()) {
            return
        }

        when (profile?.serviceType) {
            ServerProfile.TYPE_TORRENT -> passUrlToTorrentClient()
            ServerProfile.TYPE_JDOWNLOADER -> passUrlToJDownloader()
            else -> {
                // For unsupported service types, show URL in a dialog or toast
                Toast.makeText(this, "URL to download: $urlToShare", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun passUrlToTorrentClient() {
        when (profile?.torrentClientType) {
            ServerProfile.TORRENT_CLIENT_QBITTORRENT -> passUrlToQBittorrent()
            ServerProfile.TORRENT_CLIENT_TRANSMISSION -> passUrlToTransmission()
            ServerProfile.TORRENT_CLIENTUTORRENT -> passUrlToUTorrent()
            else -> {
                Toast.makeText(this, "URL to download: $urlToShare", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun passUrlToQBittorrent() {
        val addTorrentScript = """
            (function() {
                var url = '${urlToShare?.replace("'", "\\'")}';

                // Try to find and fill the add torrent URL field
                function fillAddTorrentField() {
                    // Common selectors for qBittorrent add torrent dialog
                    var urlField = document.querySelector('input[name="urls"], textarea[name="urls"], input[placeholder*="URL"], textarea[placeholder*="URL"]');

                    if (urlField) {
                        urlField.value = url;
                        urlField.focus();

                        // Trigger change event
                        var event = new Event('change', { bubbles: true });
                        urlField.dispatchEvent(event);

                        return true;
                    }

                    // If no field found, try to click Add Torrent button first
                    var addButton = document.querySelector('a:contains("Add Torrent"), button:contains("Add"), input[value*="Add"]');
                    if (addButton) {
                        addButton.click();
                        setTimeout(fillAddTorrentField, 1000);
                        return true;
                    }

                    return false;
                }

                if (!fillAddTorrentField()) {
                    setTimeout(fillAddTorrentField, 1000);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(addTorrentScript) { result ->
            Console.log("qBittorrent URL passing script executed: $result")
            Toast.makeText(this, "URL passed to qBittorrent: $urlToShare", Toast.LENGTH_SHORT).show()
        }
    }

    private fun passUrlToTransmission() {
        val addTorrentScript = """
            (function() {
                var url = '${urlToShare?.replace("'", "\\'")}';

                function addToTransmission() {
                    // Look for Transmission's add torrent functionality
                    var addButton = document.querySelector('button[title*="Add"], .toolbar-add, #toolbar-add');
                    if (addButton) {
                        addButton.click();

                        setTimeout(function() {
                            var urlField = document.querySelector('input[type="text"], textarea');
                            if (urlField) {
                                urlField.value = url;
                                urlField.focus();
                            }
                        }, 500);
                        return true;
                    }
                    return false;
                }

                if (!addToTransmission()) {
                    setTimeout(addToTransmission, 1000);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(addTorrentScript) { result ->
            Console.log("Transmission URL passing script executed: $result")
            Toast.makeText(this, "URL passed to Transmission: $urlToShare", Toast.LENGTH_SHORT).show()
        }
    }

    private fun passUrlToUTorrent() {
        // Similar implementation for uTorrent
        Toast.makeText(this, "URL to add: $urlToShare", Toast.LENGTH_LONG).show()
    }

    private fun passUrlToJDownloader() {
        // JDownloader URL passing implementation
        Toast.makeText(this, "URL to add: $urlToShare", Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.web_ui_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                webView?.reload()
                true
            }
            R.id.action_open_browser -> {
                // Open in external browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${profile?.url}:${profile?.port}"))
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()

        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this)
        if (themeManager!!.hasThemeChanged()) {
            themeManager!!.resetThemeChangedFlag()
            recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
    }
}