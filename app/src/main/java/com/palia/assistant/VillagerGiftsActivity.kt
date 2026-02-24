package com.palia.assistant

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import kotlin.concurrent.thread

class VillagerGiftsActivity : AppCompatActivity() {

    data class Villager(val name: String, val likes: String, val imgUrl: String)

    private val villagers = listOf(
        Villager("Ashura", "Sundrop Lilies, Wild Garlic, Fish (Any), Heartwood", "https://palia.wiki.gg/images/thumb/a/a1/Ashura_Profile.png/120px-Ashura_Profile.png"),
        Villager("Auni", "Bugs (Any), Silk Thread, Smoke Bombs", "https://palia.wiki.gg/images/thumb/2/23/Auni_Profile.png/120px-Auni_Profile.png"),
        Villager("Badruu", "Crops, Fertilizer, Apples, Blueberries", "https://palia.wiki.gg/images/thumb/1/1b/Badruu_Profile.png/120px-Badruu_Profile.png"),
        Villager("Caleri", "Pickled Veggies, Fur, Chapaa Meat, Books", "https://palia.wiki.gg/images/thumb/8/87/Caleri_Profile.png/120px-Caleri_Profile.png"),
        Villager("Chayne", "Crystal Lake Lotus, Ores, Shells, Sundrop Lilies", "https://palia.wiki.gg/images/thumb/a/a5/Chayne_Profile.png/120px-Chayne_Profile.png"),
        Villager("Delaila", "Crops, Fruit Jams, Sweet Treats", "https://palia.wiki.gg/images/thumb/2/23/Delaila_Profile.png/120px-Delaila_Profile.png"),
        Villager("Einar", "Fish (Any), Shiny Pebbles, Vegetables", "https://palia.wiki.gg/images/thumb/1/1a/Einar_Profile.png/120px-Einar_Profile.png"),
        Villager("Hassian", "Arrows, Fish, Chapaa Meat, Fireworks", "https://palia.wiki.gg/images/thumb/4/4b/Hassian_Profile.png/120px-Hassian_Profile.png"),
        Villager("Hodari", "Ores, Bars, Seeds, Pickled Veggies", "https://palia.wiki.gg/images/thumb/9/91/Hodari_Profile.png/120px-Hodari_Profile.png"),
        Villager("Jel", "Bugs, Fabrics, Fish, Pearls", "https://palia.wiki.gg/images/thumb/1/1f/Jel_Profile.png/120px-Jel_Profile.png"),
        Villager("Jina", "Mushrooms, Ores, Relics", "https://palia.wiki.gg/images/thumb/c/ca/Jina_Profile.png/120px-Jina_Profile.png"),
        Villager("Reth", "Sweet Treats, Fish, Soups, Crops", "https://palia.wiki.gg/images/thumb/6/65/Reth_Profile.png/120px-Reth_Profile.png"),
        Villager("Tish", "Flowers, Wood, Furniture, Shells", "https://palia.wiki.gg/images/thumb/b/ba/Tish_Profile.png/120px-Tish_Profile.png"),
        Villager("Zeki", "Fish, Bugs, Meals, Ores", "https://palia.wiki.gg/images/thumb/5/5e/Zeki_Profile.png/120px-Zeki_Profile.png")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_villager_gifts)
        supportActionBar?.title = "Villager Gift Guide"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val container = findViewById<LinearLayout>(R.id.giftsContainer)
        val density = resources.displayMetrics.density

        for (villager in villagers) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1A1638"))
                    cornerRadius = 32f
                }
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (16 * density).toInt())
                }
                gravity = Gravity.CENTER_VERTICAL
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (70 * density).toInt(),
                    (70 * density).toInt()
                ).apply {
                    setMargins(0, 0, (16 * density).toInt(), 0)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Load Image in background thread directly from Palia Wiki
            thread {
                try {
                    val url = URL(villager.imgUrl)
                    val bmp = BitmapFactory.decodeStream(url.openStream())
                    runOnUiThread {
                        imageView.setImageBitmap(bmp)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val textContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val nameText = TextView(this).apply {
                text = villager.name
                setTextColor(Color.parseColor("#6A5ACD"))
                textSize = 20f
                paint.isFakeBoldText = true
            }

            val likesLabel = TextView(this).apply {
                text = "Always Loves:"
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 12f
                paint.isFakeBoldText = true
                setPadding(0, (4 * density).toInt(), 0, (2 * density).toInt())
            }

            val likesText = TextView(this).apply {
                text = villager.likes
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            textContainer.addView(nameText)
            textContainer.addView(likesLabel)
            textContainer.addView(likesText)

            card.addView(imageView)
            card.addView(textContainer)
            container.addView(card)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
