package com.palia.assistant

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Setup Database & File Storage Foundation
        prefs = getSharedPreferences("PaliaAssistantSettings", Context.MODE_PRIVATE)
        val appStorageDir = File(filesDir, "PaliaData")
        if (!appStorageDir.exists()) appStorageDir.mkdirs()

        // 2. Initialize UI
        webView = findViewById(R.id.wikiWebView)
        val searchField = findViewById<EditText>(R.id.searchWiki)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        // 3. Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient() // Opens links inside the app
        
        // 4. Auto-load Wiki Homepage
        webView.loadUrl("https://palia.wiki.gg/")

        // 5. Search Logic
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
            } else {
                false
            }
        }
    }

    // Allow hardware back button to navigate wiki history
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
