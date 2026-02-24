package com.palia.assistant

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.util.Calendar

class NotesActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var notesContainer: LinearLayout
    private lateinit var etNoteInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        supportActionBar?.title = "Palia Notepad"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefs = getSharedPreferences("PaliaNotesDB", Context.MODE_PRIVATE)
        notesContainer = findViewById(R.id.notesContainer)
        etNoteInput = findViewById(R.id.etNoteInput)

        findViewById<Button>(R.id.btnSaveNote).setOnClickListener {
            val text = etNoteInput.text.toString().trim()
            if (text.isNotEmpty()) {
                saveNote(text)
                etNoteInput.text.clear()
                loadNotes()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        loadNotes()
    }

    private fun saveNote(note: String) {
        val notesList = getSavedNotes().toMutableList()
        notesList.add(note)
        prefs.edit().putString("notes_array", JSONArray(notesList).toString()).apply()
    }

    private fun deleteNote(index: Int) {
        val notesList = getSavedNotes().toMutableList()
        if (index in notesList.indices) {
            notesList.removeAt(index)
            prefs.edit().putString("notes_array", JSONArray(notesList).toString()).apply()
            loadNotes()
        }
    }

    private fun getSavedNotes(): List<String> {
        val jsonString = prefs.getString("notes_array", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    private fun loadNotes() {
        notesContainer.removeAllViews()
        val notes = getSavedNotes()
        for ((index, note) in notes.withIndex()) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#1A1638"))
                setPadding(24, 24, 24, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val tvNote = TextView(this).apply {
                text = note
                setTextColor(Color.WHITE)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            card.addView(tvNote)

            val btnLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 24, 0, 0) }
            }

            val btnRemind = Button(this).apply {
                text = "Remind Me"
                setBackgroundColor(Color.parseColor("#302B63"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 12, 0)
                }
                setOnClickListener { showTimePickerForReminder(note) }
            }

            val btnDelete = Button(this).apply {
                text = "Delete"
                setBackgroundColor(Color.parseColor("#8B0000"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(12, 0, 0, 0)
                }
                setOnClickListener { deleteNote(index) }
            }

            btnLayout.addView(btnRemind)
            btnLayout.addView(btnDelete)
            card.addView(btnLayout)
            notesContainer.addView(card)
        }
    }

    private fun showTimePickerForReminder(noteText: String) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1) 
                }
            }
            setReminder(noteText, alarmTime.timeInMillis)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun setReminder(noteText: String, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("NOTE_TEXT", noteText)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            noteText.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            Toast.makeText(this, "Reminder notification set!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission missing for alarms.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
