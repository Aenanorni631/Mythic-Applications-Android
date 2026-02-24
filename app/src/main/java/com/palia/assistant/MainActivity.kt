package com.palia.assistant

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var favPrefs: SharedPreferences
    private var currentUrl: String = "https://palia.wiki.gg/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        favPrefs = getSharedPreferences("PaliaFavorites", Context.MODE_PRIVATE)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            android.R.string.ok, android.R.string.cancel
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
        
        // Tool 1: Saved Links
        val btnSavedLinksTool = navView.findViewById<Button>(R.id.btnSavedLinksTool)
        btnSavedLinksTool.setOnClickListener {
            startActivity(Intent(this, SavedLinksActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Tool 2: Official Patch Notes
        val btnPatchNotesTool = navView.findViewById<Button>(R.id.btnPatchNotesTool)
        btnPatchNotesTool.setOnClickListener {
            webView.loadUrl("https://palia.wiki.gg/wiki/Patch_Notes")
            drawerLayout.closeDrawer(GravityCompat.START)
            findViewById<EditText>(R.id.searchWiki).clearFocus()
        }

        // Tool 3: Interactive Map
        val btnInteractiveMapTool = navView.findViewById<Button>(R.id.btnInteractiveMapTool)
        btnInteractiveMapTool.setOnClickListener {
            webView.loadUrl("https://palia.interactivemap.app/")
            drawerLayout.closeDrawer(GravityCompat.START)
            findViewById<EditText>(R.id.searchWiki).clearFocus()
        }

        webView = findViewById(R.id.wikiWebView)
        val searchField = findViewById<EditText>(R.id.searchWiki)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        // --- PERFORMANCE ENHANCEMENTS ---
        
        // Force Hardware Acceleration for the WebView
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true // Allows map databases to be cached locally
        
        // Caching and Viewport Optimizations
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        
        // Allow map to fetch assets properly without security blocks
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        
        // ---------------------------------

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                currentUrl = url ?: ""
                invalidateOptionsMenu() 
            }
        }

        val urlToLoad = intent.getStringExtra("LOAD_URL") ?: currentUrl
        webView.loadUrl(urlToLoad)

        val performSearch = {
            val query = searchField.text.toString().trim()
            if (query.isNotEmpty()) {
                webView.loadUrl("https://palia.wiki.gg/index.php?search=$query")
                searchField.clearFocus()
            }
        }

        btnSearch.setOnClickListener { performSearch() }
        searchField.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSearch()
                true
            } else false
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra("LOAD_URL")?.let {
            webView.loadUrl(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val favItem = menu?.findItem(R.id.action_favorite)
        if (favPrefs.contains(currentUrl)) {
            favItem?.setIcon(R.drawable.ic_star_filled)
        } else {
            favItem?.setIcon(R.drawable.ic_star_outline)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_favorite -> {
                toggleFavorite()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleFavorite() {
        if (favPrefs.contains(currentUrl)) {
            favPrefs.edit().remove(currentUrl).apply()
            invalidateOptionsMenu()
        } else {
            showSaveDialog()
        }
    }

    private fun showSaveDialog() {
        val input = EditText(this)
        input.hint = "Custom Name (e.g. Flow Trees)"
        input.setText(webView.title)

        AlertDialog.Builder(this)
            .setTitle("Save Wiki Page")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val customName = input.text.toString().trim()
                if (customName.isNotEmpty()) {
                    favPrefs.edit().putString(currentUrl, customName).apply()
                    invalidateOptionsMenu()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
