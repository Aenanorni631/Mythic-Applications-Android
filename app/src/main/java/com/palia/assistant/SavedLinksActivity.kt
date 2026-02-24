package com.palia.assistant

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SavedLinksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_links)
        supportActionBar?.title = "Saved Wiki Links"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val container = findViewById<LinearLayout>(R.id.savedLinksContainer)
        val favPrefs = getSharedPreferences("PaliaFavorites", Context.MODE_PRIVATE)
        val allFavs = favPrefs.all

        if (allFavs.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "No saved links yet.\n\nTap the star icon on any wiki page to save it here."
                textSize = 16f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 64, 0, 0)
            })
            return
        }

        for ((url, title) in allFavs) {
            // Row Container
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 32, 24, 32)
                setBackgroundResource(android.R.drawable.list_selector_background)
            }

            // Title Text
            val titleView = TextView(this).apply {
                text = "⭐ $title"
                textSize = 18f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setOnClickListener {
                    val intent = Intent(this@SavedLinksActivity, MainActivity::class.java)
                    intent.putExtra("LOAD_URL", url)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            }

            // Sleek Trash Icon (instead of giant red X)
            val deleteBtn = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.parseColor("#FF5252")) // Soft Red
                setBackgroundResource(android.R.color.transparent)
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    favPrefs.edit().remove(url).apply()
                    container.removeView(row)
                    if (favPrefs.all.isEmpty()) recreate() // Refresh if empty
                }
            }

            row.addView(titleView)
            row.addView(deleteBtn)
            container.addView(row)
            
            // Add a subtle divider line
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                setBackgroundColor(Color.parseColor("#302B63")) // palia_secondary
            }
            container.addView(divider)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
