package com.palia.assistant

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class VillagerGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_villager_guide)
        supportActionBar?.title = "Villager Gift Guide"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = findViewById<WebView>(R.id.giftGuideWebView)
        webView.setBackgroundColor(0x00000000) // Transparent background
        webView.settings.javaScriptEnabled = true
        
        // Loads the beautiful HTML file we created in the assets folder instantly
        webView.loadUrl("file:///android_asset/villager_gifts.html")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
