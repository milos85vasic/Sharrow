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

            // Wait for authentication to complete and verify login success
            webView?.postDelayed({
                verifyAuthenticationAndPassUrl()
            }, 3000) // Wait for authentication
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

    private fun verifyAuthenticationAndPassUrl() {
        val verificationScript = """
            (function() {
                // Check if we're successfully logged in by looking for main UI elements
                var isLoggedIn = document.querySelector('.mainmenu, #desktop, .toolbar, #torrentsTable, .torrentTable, .main-panel, #mainContent, .content') !== null;

                // Also check if we're still on a login page
                var isOnLoginPage = document.querySelector('input[name="username"], input[name="password"], .login-form') !== null;

                console.log('Login verification - isLoggedIn: ' + isLoggedIn + ', isOnLoginPage: ' + isOnLoginPage);

                return isLoggedIn && !isOnLoginPage;
            })();
        """.trimIndent()

        webView?.evaluateJavascript(verificationScript) { result ->
            val isAuthenticated = result.trim() == "true"
            Console.log("Authentication verification result: $result (authenticated: $isAuthenticated)")

            if (isAuthenticated) {
                this.isAuthenticated = true
                passUrlToWebUI()
            } else {
                // Authentication may have failed, try API approach or wait longer
                Console.log("Authentication verification failed, trying API approach or extending wait")
                webView?.postDelayed({
                    // Try API approach first, then fallback to UI automation
                    tryQBittorrentApiApproach()
                }, 2000)
            }
        }
    }

    private fun tryQBittorrentApiApproach() {
        if (profile?.serviceType != ServerProfile.TYPE_TORRENT ||
            profile?.torrentClientType != ServerProfile.TORRENT_CLIENT_QBITTORRENT ||
            urlToShare.isNullOrEmpty()) {

            // Fall back to UI automation
            passUrlToWebUI()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = addTorrentViaQBittorrentApi()
                withContext(Dispatchers.Main) {
                    if (success) {
                        val message = if (urlToShare!!.startsWith("magnet:")) {
                            "Magnet link successfully added via qBittorrent API"
                        } else {
                            "Torrent URL successfully added via qBittorrent API"
                        }
                        Toast.makeText(this@WebUIActivity, message, Toast.LENGTH_LONG).show()
                    } else {
                        // Fall back to UI automation
                        Console.log("API approach failed, falling back to UI automation")
                        passUrlToWebUI()
                    }
                }
            } catch (e: Exception) {
                Console.error(e, "Error using qBittorrent API")
                withContext(Dispatchers.Main) {
                    // Fall back to UI automation
                    passUrlToWebUI()
                }
            }
        }
    }

    private fun addTorrentViaQBittorrentApi(): Boolean {
        return try {
            val client = OkHttpClient()
            val baseUrl = "${profile?.url}:${profile?.port}"

            // First try to login via API
            val loginData = FormBody.Builder()
                .add("username", profile?.username ?: "")
                .add("password", profile?.password ?: "")
                .build()

            val loginRequest = Request.Builder()
                .url("$baseUrl/api/v2/auth/login")
                .post(loginData)
                .build()

            val loginResponse = client.newCall(loginRequest).execute()
            val cookies = loginResponse.headers("Set-Cookie")

            if (loginResponse.isSuccessful && cookies.isNotEmpty()) {
                // Use cookies for authenticated request
                val cookieString = cookies.joinToString("; ") { it.split(";")[0] }

                // Add torrent via API
                val torrentData = FormBody.Builder()
                    .add("urls", urlToShare ?: "")
                    .build()

                val addRequest = Request.Builder()
                    .url("$baseUrl/api/v2/torrents/add")
                    .addHeader("Cookie", cookieString)
                    .post(torrentData)
                    .build()

                val addResponse = client.newCall(addRequest).execute()
                Console.log("qBittorrent API add response: ${addResponse.code}")

                return addResponse.isSuccessful
            } else {
                Console.log("qBittorrent API login failed: ${loginResponse.code}")
                return false
            }
        } catch (e: Exception) {
            Console.error(e, "Exception in qBittorrent API approach")
            false
        }
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
                var maxAttempts = 20;
                var dialogFound = false;

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
                        'input[placeholder*="magnet"]',
                        'textarea[placeholder*="URL"]',
                        'textarea[placeholder*="url"]',
                        'textarea[placeholder*="magnet"]',
                        'input[id*="url"]',
                        'textarea[id*="url"]',
                        '#urls',
                        '#url',
                        '.torrent-url',
                        'input[type="url"]',
                        'textarea[rows]',
                        '.form-control[placeholder*="URL"]'
                    ].join(', '));

                    if (urlField && urlField.offsetParent !== null) { // Check if visible
                        console.log('Found URL field, filling with torrent URL');
                        dialogFound = true;

                        // Clear any existing content
                        urlField.value = '';
                        urlField.focus();

                        // Fill with our URL
                        urlField.value = url;

                        // Trigger comprehensive events to ensure proper form handling
                        var events = ['input', 'change', 'paste', 'keyup', 'blur', 'focus'];
                        events.forEach(function(eventType) {
                            try {
                                var event = new Event(eventType, { bubbles: true, cancelable: true });
                                urlField.dispatchEvent(event);
                            } catch (e) {
                                console.log('Event dispatch failed for ' + eventType + ': ' + e.message);
                            }
                        });

                        // Also try keyboard events for form validation
                        try {
                            var keyEvent = new KeyboardEvent('keydown', { key: 'Enter', keyCode: 13, bubbles: true });
                            urlField.dispatchEvent(keyEvent);
                        } catch (e) {
                            console.log('Keyboard event failed: ' + e.message);
                        }

                        // Look for download/add/OK button to submit
                        setTimeout(function() {
                            var submitButtons = document.querySelectorAll([
                                'input[type="submit"]',
                                'button[type="submit"]',
                                'input[value*="Download"]',
                                'button[onclick*="add"]',
                                'button[onclick*="download"]',
                                'input[value*="Add"]',
                                'button:contains("Add")',
                                'button:contains("OK")',
                                'button:contains("Download")',
                                '.btn-primary',
                                '.btn-success',
                                '#downloadButton',
                                '.dialog-confirm',
                                '.modal-footer button'
                            ].join(', '));

                            for (var i = 0; i < submitButtons.length; i++) {
                                var btn = submitButtons[i];
                                if (btn.offsetParent !== null && !btn.disabled) {
                                    console.log('Clicking submit button: ' + btn.outerHTML.substring(0, 150));
                                    btn.click();
                                    return;
                                }
                            }

                            console.log('No submit button found, URL has been pasted');
                        }, 800);

                        return true;
                    }

                    // If we haven't found a dialog yet, try to open one
                    if (!dialogFound) {
                        // Try to find modern qBittorrent UI "Add Torrent" button
                        var addButtons = document.querySelectorAll([
                            // Modern qBittorrent WebUI selectors
                            'button[title*="Add"]',
                            'a[title*="Add"]',
                            '.toolbar button[title*="Add"]',
                            '.toolbar a[title*="Add"]',
                            'button[onclick*="add"]',
                            'a[onclick*="add"]',
                            // Icon-based buttons
                            '.fa-plus',
                            '.material-icons:contains("add")',
                            'button .fa-plus',
                            'a .fa-plus',
                            // Text-based buttons
                            'button:contains("Add")',
                            'a:contains("Add")',
                            // Common IDs and classes
                            '#addTorrent',
                            '#add',
                            '.add-torrent',
                            '.toolbar-add',
                            '.add-button',
                            // Menu items
                            '.menu-item[onclick*="add"]',
                            '.dropdown-item[onclick*="add"]'
                        ].join(', '));

                        console.log('Found ' + addButtons.length + ' potential add buttons');

                        for (var i = 0; i < addButtons.length; i++) {
                            var button = addButtons[i];
                            if (button.offsetParent !== null && !button.disabled) {
                                var buttonText = button.textContent || button.title || button.getAttribute('title') || '';
                                console.log('Trying button: ' + buttonText + ' | ' + button.outerHTML.substring(0, 200));

                                button.click();

                                // Wait for dialog to appear
                                setTimeout(addTorrentUrl, 2000);
                                return true;
                            }
                        }

                        // Try right-click context menu
                        var torrentTable = document.querySelector('#torrentsTable, .torrentTable, #mainContent, .torrent-list, .content');
                        if (torrentTable) {
                            console.log('Trying right-click on torrent table');
                            var rightClick = new MouseEvent('contextmenu', {
                                bubbles: true,
                                cancelable: true,
                                button: 2
                            });
                            torrentTable.dispatchEvent(rightClick);

                            setTimeout(function() {
                                var contextMenu = document.querySelector('.context-menu, .contextmenu');
                                if (contextMenu) {
                                    var addItem = contextMenu.querySelector('[onclick*="add"], :contains("Add")');
                                    if (addItem) {
                                        addItem.click();
                                        setTimeout(addTorrentUrl, 1500);
                                    }
                                }
                            }, 500);

                            return true;
                        }
                    }

                    if (attempts < maxAttempts) {
                        setTimeout(addTorrentUrl, 1500);
                    } else {
                        console.log('Max attempts reached. Final attempt: showing manual prompt');
                        // Create a more prominent notification
                        var notification = document.createElement('div');
                        notification.style.cssText = `
                            position: fixed; top: 20px; right: 20px; z-index: 10000;
                            background: #f44336; color: white; padding: 15px 20px;
                            border-radius: 8px; font-family: Arial, sans-serif;
                            font-size: 14px; box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                            max-width: 400px; word-wrap: break-word;
                        `;
                        notification.innerHTML = `
                            <strong>ShareConnect:</strong><br>
                            Please manually add this magnet/torrent:<br>
                            <div style="background: rgba(255,255,255,0.2); padding: 8px; margin: 8px 0; border-radius: 4px; font-size: 12px; word-break: break-all;">
                                ` + url + `
                            </div>
                            <button onclick="this.parentElement.remove();" style="background: none; border: 1px solid white; color: white; padding: 4px 8px; border-radius: 4px; cursor: pointer; float: right;">Close</button>
                        `;
                        document.body.appendChild(notification);

                        // Auto-remove after 15 seconds
                        setTimeout(function() {
                            if (notification.parentElement) {
                                notification.remove();
                            }
                        }, 15000);
                    }
                    return false;
                }

                // Start the process
                setTimeout(addTorrentUrl, 1000); // Initial delay to ensure page is ready
            })();
        """.trimIndent()

        webView?.evaluateJavascript(addTorrentScript) { result ->
            Console.log("qBittorrent enhanced URL passing script executed: $result")

            // Don't show success message immediately since we don't know if it succeeded
            // The script itself will show appropriate feedback
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