package com.palia.assistant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class QuickReferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_reference)
        supportActionBar?.title = "Quick Reference"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
