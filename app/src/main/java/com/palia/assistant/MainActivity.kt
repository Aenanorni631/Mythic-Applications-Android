package com.palia.assistant

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
        val btnSavedLinksTool = navView.findViewById<Button>(R.id.btnSavedLinksTool)
        btnSavedLinksTool.setOnClickListener {
            openSavedLinksMenu()
        }

        webView = findViewById(R.id.wikiWebView)
        val searchField = findViewById<EditText>(R.id.searchWiki)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                currentUrl = url ?: ""
                invalidateOptionsMenu() 
            }
        }
        webView.loadUrl(currentUrl)

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

    private fun openSavedLinksMenu() {
        val dialogView = ScrollView(this)
        val container = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40) 
        }
        dialogView.addView(container)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Saved Wiki Links")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        val allFavs = favPrefs.all
        if (allFavs.isEmpty()) {
            container.addView(TextView(this).apply { 
                text = "No saved links found. Press the star icon on a wiki page to save it."
                textSize = 16f
                setTextColor(Color.GRAY)
            })
        } else {
            for ((url, title) in allFavs) {
                val row = LinearLayout(this).apply { 
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 16, 0, 16)
                    gravity = Gravity.CENTER_VERTICAL
                }
                
                val titleView = TextView(this).apply {
                    text = "⭐ $title"
                    textSize = 18f
                    setTextColor(Color.WHITE) // Changed to white for readability
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setOnClickListener {
                        webView.loadUrl(url)
                        dialog.dismiss()
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
                
                val deleteBtn = Button(this).apply {
                    text = "X"
                    setBackgroundColor(Color.RED)
                    setTextColor(Color.WHITE)
                    // Made button smaller and added padding
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(16, 0, 0, 0)
                    }
                    setPadding(24, 0, 24, 0)
                    minWidth = 0
                    minHeight = 0
                    setOnClickListener {
                        favPrefs.edit().remove(url).apply()
                        container.removeView(row)
                        invalidateOptionsMenu()
                        if (favPrefs.all.isEmpty()) dialog.dismiss()
                    }
                }
                
                row.addView(titleView)
                row.addView(deleteBtn)
                container.addView(row)
            }
        }
        dialog.show()
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
