package com.kdr.nuven

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSpinner: Spinner
    private lateinit var toolbarPositionSpinner: Spinner
    private lateinit var fontSpinner: Spinner
    private lateinit var licensesTextView: TextView
    
    private val json = Json { ignoreUnknownKeys = true }

    private val themes = arrayOf("Teletext", "Raven")
    private val toolbarPositions = arrayOf("Bottom", "Top")
    private val fonts = arrayOf("System Monospace", "JetBrains Mono", "VT323 (Retro)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeSpinner = findViewById(R.id.themeSpinner)
        toolbarPositionSpinner = findViewById(R.id.toolbarPositionSpinner)
        fontSpinner = findViewById(R.id.fontSpinner)
        licensesTextView = findViewById(R.id.licensesTextView)
        
        // Setup licenses text
        setupLicenses()

        // Setup theme spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        // Load current settings
        val currentTheme = SettingsManager.getTheme(this)
        themeSpinner.setSelection(themes.indexOf(currentTheme))

        // Set up listener to save theme when selection changes
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedTheme = themes[position]
                SettingsManager.setTheme(this@SettingsActivity, selectedTheme)
                
                // Theme will be applied when returning to main activity
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup toolbar position spinner
        val positionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, toolbarPositions)
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toolbarPositionSpinner.adapter = positionAdapter

        // Load current toolbar position
        val currentPosition = SettingsManager.getToolbarPosition(this)
        val positionIndex = if (currentPosition == "top") 1 else 0
        toolbarPositionSpinner.setSelection(positionIndex)

        // Set up listener to save toolbar position when selection changes
        toolbarPositionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedPosition = if (position == 0) "bottom" else "top"
                SettingsManager.setToolbarPosition(this@SettingsActivity, selectedPosition)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup font spinner
        val fontAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts)
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontSpinner.adapter = fontAdapter
        
        // Load current font
        val currentFont = SettingsManager.getFont(this)
        fontSpinner.setSelection(fonts.indexOf(currentFont))
        
        // Set up listener to save font when selection changes
        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedFont = fonts[position]
                SettingsManager.setFont(this@SettingsActivity, selectedFont)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupLicenses() {
        // Extract unique sources from articles
        val sources = extractArticleSources()
        val sourcesHtml = if (sources.isNotEmpty()) {
            sources.sorted().joinToString("<br/>") { "• $it" }
        } else {
            "Loading article sources..."
        }
        
        val licensesText = """
            <b>JetBrains Mono</b><br/>
            Copyright 2020 The JetBrains Mono Project Authors<br/>
            Licensed under the SIL Open Font License 1.1<br/>
            <a href="https://github.com/JetBrains/JetBrainsMono">https://github.com/JetBrains/JetBrainsMono</a>
            <br/><br/>
            
            <b>VT323</b><br/>
            Copyright (c) 2011 Peter Hull<br/>
            Licensed under the SIL Open Font License 1.1<br/>
            <a href="https://github.com/phoikoi/VT323">https://github.com/phoikoi/VT323</a>
            <br/><br/>
            
            <b>Kotlin</b><br/>
            Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors<br/>
            Licensed under the Apache License 2.0<br/>
            <a href="https://kotlinlang.org">https://kotlinlang.org</a>
            <br/><br/>
            
            <b>Android SDK</b><br/>
            Copyright (C) The Android Open Source Project<br/>
            Licensed under the Apache License 2.0<br/>
            <a href="https://source.android.com">https://source.android.com</a>
            <br/><br/>
            
            <b>AndroidX Libraries</b><br/>
            Copyright (C) The Android Open Source Project<br/>
            Licensed under the Apache License 2.0<br/>
            <a href="https://developer.android.com/jetpack/androidx">https://developer.android.com/jetpack/androidx</a>
            <br/><br/>
            
            <b>Material Components for Android</b><br/>
            Copyright (C) Google Inc.<br/>
            Licensed under the Apache License 2.0<br/>
            <a href="https://github.com/material-components/material-components-android">Material Components</a>
            <br/><br/>
            
            <b>Article Content Sources</b><br/>
            Content aggregated from the following news sources:<br/>
            $sourcesHtml
            <br/><br/>
            Articles are fetched and curated from public RSS feeds and web sources.<br/>
            All content belongs to their respective publishers.<br/>
            Data repository: <a href="${AppConfig.githubRepoUrl}">${AppConfig.githubRepoUrl}</a>
        """.trimIndent()
        
        licensesTextView.text = Html.fromHtml(licensesText, Html.FROM_HTML_MODE_COMPACT)
        licensesTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
    
    private fun extractArticleSources(): Set<String> {
        val sources = mutableSetOf<String>()
        
        try {
            // Get the JSON data from DataManager
            val jsonData = DataManager.pages
            
            if (jsonData != null) {
                // Parse the data structure
                val teletextData = json.decodeFromString<TeletextData>(jsonData)
                
                // Extract unique sources from all articles
                teletextData.articles.forEach { article ->
                    article.source?.let { source ->
                        if (source.isNotBlank()) {
                            sources.add(source)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return sources
    }
}