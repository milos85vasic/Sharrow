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

        // Enhanced JavaScript to auto-fill and submit login form with better error handling
        val loginScript = """
            (function() {
                var attempts = 0;
                var maxAttempts = 10;

                // Wait for login form to be available
                function waitForLogin() {
                    attempts++;
                    console.log('qBittorrent authentication attempt: ' + attempts);

                    var usernameField = document.querySelector('input[name="username"], input[id="username"], input[type="text"], #username');
                    var passwordField = document.querySelector('input[name="password"], input[id="password"], input[type="password"], #password');
                    var loginButton = document.querySelector('input[type="submit"], button[type="submit"], #login, .login-button, input[value*="Login"], button[onclick*="login"]');

                    if (usernameField && passwordField) {
                        console.log('Found login fields, filling credentials');
                        usernameField.value = '${profile?.username?.replace("'", "\\'")}';
                        passwordField.value = '${profile?.password?.replace("'", "\\'")}';

                        // Trigger input events
                        usernameField.dispatchEvent(new Event('input', { bubbles: true }));
                        passwordField.dispatchEvent(new Event('input', { bubbles: true }));

                        // Try multiple ways to submit
                        if (loginButton) {
                            console.log('Clicking login button');
                            loginButton.click();
                        } else {
                            // Try to submit the form
                            var form = usernameField.closest('form') || passwordField.closest('form');
                            if (form) {
                                console.log('Submitting login form');
                                form.submit();
                            } else {
                                // Try pressing enter on password field
                                var enterEvent = new KeyboardEvent('keydown', { key: 'Enter', keyCode: 13 });
                                passwordField.dispatchEvent(enterEvent);
                            }
                        }
                        return true;
                    }

                    // Check if already logged in (no login form visible)
                    if (document.querySelector('.mainmenu, #desktop, .toolbar, #torrentsTable, .torrentTable')) {
                        console.log('Already logged in or login form not needed');
                        return true;
                    }

                    if (attempts < maxAttempts) {
                        setTimeout(waitForLogin, 1000);
                    } else {
                        console.log('Max authentication attempts reached');
                    }
                    return false;
                }

                waitForLogin();
            })();
        """.trimIndent()

        webView?.evaluateJavascript(loginScript) { result ->
            Console.log("qBittorrent authentication script executed: $result")
            isAuthenticated = true

            // Wait for authentication to complete, then pass URL
            webView?.postDelayed({
                passUrlToWebUI()
            }, 4000) // Increased wait time for authentication
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
                var url = '${urlToShare?.replace("'", "\\'")?.replace("\n", "\\n")}';
                var attempts = 0;
                var maxAttempts = 15;

                console.log('Attempting to add URL to qBittorrent: ' + url);

                // Enhanced function to find and fill the add torrent URL field
                function addTorrentUrl() {
                    attempts++;
                    console.log('qBittorrent add torrent attempt: ' + attempts);

                    // First check if add torrent dialog is already open
                    var urlField = document.querySelector([
                        'input[name="urls"]',
                        'textarea[name="urls"]',
                        'input[placeholder*="URL"]',
                        'input[placeholder*="url"]',
                        'textarea[placeholder*="URL"]',
                        'textarea[placeholder*="url"]',
                        'input[id*="url"]',
                        'textarea[id*="url"]',
                        '#urls',
                        '.torrent-url',
                        'input[type="url"]'
                    ].join(', '));

                    if (urlField) {
                        console.log('Found URL field, filling with torrent URL');
                        urlField.value = url;
                        urlField.focus();

                        // Trigger multiple events to ensure the field is properly updated
                        ['input', 'change', 'blur', 'paste'].forEach(function(eventType) {
                            var event = new Event(eventType, { bubbles: true });
                            urlField.dispatchEvent(event);
                        });

                        // Look for download/add button to submit
                        var submitButton = document.querySelector([
                            'input[type="submit"]',
                            'button[type="submit"]',
                            'input[value*="Download"]',
                            'button:contains("Download")',
                            'input[value*="Add"]',
                            'button:contains("Add")',
                            '.btn-primary',
                            '#downloadButton'
                        ].join(', '));

                        if (submitButton) {
                            console.log('Found submit button, clicking to add torrent');
                            setTimeout(function() {
                                submitButton.click();
                            }, 500);
                        }

                        return true;
                    }

                    // Try to find and click the "Add Torrent" button or similar
                    var addButtons = document.querySelectorAll([
                        'a[title*="Add"]',
                        'button[title*="Add"]',
                        'a:contains("Add")',
                        'button:contains("Add")',
                        '.toolbar-add',
                        '#addTorrent',
                        '.add-torrent',
                        'a[href*="add"]',
                        'button[onclick*="add"]',
                        '.fa-plus'
                    ].join(', '));

                    for (var i = 0; i < addButtons.length; i++) {
                        var button = addButtons[i];
                        if (button.offsetWidth > 0 && button.offsetHeight > 0) { // Check if visible
                            console.log('Clicking add torrent button: ' + button.outerHTML.substring(0, 100));
                            button.click();
                            setTimeout(addTorrentUrl, 1500); // Wait for dialog to appear
                            return true;
                        }
                    }

                    // Look for menu items or dropdowns
                    var menuItems = document.querySelectorAll([
                        '.menu-item',
                        '.dropdown-item',
                        'li a'
                    ].join(', '));

                    for (var i = 0; i < menuItems.length; i++) {
                        var item = menuItems[i];
                        if (item.textContent.toLowerCase().includes('add') &&
                            item.textContent.toLowerCase().includes('torrent')) {
                            console.log('Clicking menu item: ' + item.textContent);
                            item.click();
                            setTimeout(addTorrentUrl, 1500);
                            return true;
                        }
                    }

                    if (attempts < maxAttempts) {
                        setTimeout(addTorrentUrl, 1000);
                    } else {
                        console.log('Max attempts reached, showing URL in alert');
                        alert('Please manually add this torrent URL to qBittorrent: ' + url);
                    }
                    return false;
                }

                // Start the process
                addTorrentUrl();
            })();
        """.trimIndent()

        webView?.evaluateJavascript(addTorrentScript) { result ->
            Console.log("qBittorrent URL passing script executed: $result")

            // Show success message with the URL
            val message = if (urlToShare!!.startsWith("magnet:")) {
                "Magnet link added to qBittorrent"
            } else {
                "Torrent file added to qBittorrent"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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