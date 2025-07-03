package com.seuprojeto.safeshot

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        setContentView(listView)

        val prefs = getSharedPreferences("safeshot_log", MODE_PRIVATE)
        val logs = prefs.getStringSet("log_entries", emptySet())?.toList()?.sortedDescending() ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logs)
        listView.adapter = adapter
    }
} 