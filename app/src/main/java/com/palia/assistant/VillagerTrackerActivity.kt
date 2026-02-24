package com.palia.assistant

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class VillagerTrackerActivity : AppCompatActivity() {
    private lateinit var tvClock: TextView
    private lateinit var tvAmPm: TextView
    private lateinit var webView: WebView
    private lateinit var loadingOverlay: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    private val updateClockTask = object : Runnable {
        override fun run() {
            updatePaliaTime()
            handler.postDelayed(this, 1000) // Update every real-world second
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_villager_tracker)
        supportActionBar?.hide() // Hide standard action bar to show our custom clock bar

        tvClock = findViewById(R.id.tvClock)
        tvAmPm = findViewById(R.id.tvAmPm)
        webView = findViewById(R.id.trackerWebView)
        loadingOverlay = findViewById(R.id.loadingOverlayTracker)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        setupWebView()
        updatePaliaTime()
        handler.post(updateClockTask)
    }

    private fun updatePaliaTime() {
        val calendar = Calendar.getInstance()
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        
        // Math to convert real time to Palia time
        val totalSeconds = (minutes * 60) + seconds
        val paliaTotalMinutes = (totalSeconds * 0.4).toInt()
        
        val paliaHour24 = paliaTotalMinutes / 60
        val paliaMinute = paliaTotalMinutes % 60
        
        val amPm = if (paliaHour24 >= 12) "PM" else "AM"
        var paliaHour12 = paliaHour24 % 12
        if (paliaHour12 == 0) paliaHour12 = 12
        
        tvClock.text = String.format("%02d:%02d", paliaHour12, paliaMinute)
        tvAmPm.text = amPm
    }

    private fun setupWebView() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.webChromeClient = WebChromeClient()
        
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.offscreenPreRaster = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingOverlay.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingOverlay.visibility = View.GONE
            }
        }

        // Load the THGL map which features live moving NPC heads
        webView.loadUrl("https://palia.th.gl/en/palia/map")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateClockTask)
    }
}
