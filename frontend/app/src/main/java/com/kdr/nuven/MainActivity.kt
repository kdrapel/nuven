package com.kdr.nuven

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var parser: TeletextParser
    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: PageViewPagerAdapter
    private lateinit var keypadButton: ImageButton

    // Navigation bar components
    private lateinit var currentPageNumber: TextView
    private lateinit var expandToggleButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var settingsButton: ImageButton

    // Coordinate overlay
    private lateinit var coordinateOverlay: TextView

    private var isExpandedMode = false
    private var isDebugMode = false
    
    private var currentThemeName: String = ""
    private var currentToolbarPosition: String = ""
    private var currentFontName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        parser = TeletextParser()

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        keypadButton = findViewById(R.id.keypadButton)
        currentPageNumber = findViewById(R.id.currentPageNumber)
        expandToggleButton = findViewById(R.id.expandToggleButton)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        homeButton = findViewById(R.id.homeButton)
        settingsButton = findViewById(R.id.settingsButton)
        coordinateOverlay = findViewById(R.id.coordinateOverlay)

        // Load settings
        isExpandedMode = SettingsManager.isExpandModeEnabled(this)
        currentToolbarPosition = SettingsManager.getToolbarPosition(this)

        // Apply toolbar position
        applyToolbarPosition()

        // Setup ViewPager
        pageAdapter = PageViewPagerAdapter(isExpandedMode, { pageNumber ->
            navigateToPage(pageNumber)
        }, { url ->
            // Open URL in external browser
            openUrlInBrowser(url)
        }) { x, y ->
            // Update coordinate overlay when coordinates change
            if (isDebugMode) {
                coordinateOverlay.text = "X: $x, Y: $y"
            }
        }
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = 1

        // Set initial button state
        expandToggleButton.alpha = if (isExpandedMode) 1.0f else 0.6f
        (viewPager.getChildAt(0) as androidx.recyclerview.widget.RecyclerView).isNestedScrollingEnabled = !isExpandedMode

        // Listen to page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationBar(position)
            }
        })

        // Load teletext data
        DataManager.loadState.observe(this) {
            when (it) {
                is LoadResult.Success -> {
                    loadData(it.lastUpdated)
                    navigateToPage(100) // Navigate to page 100 on refresh
                }
                is LoadResult.Error -> {
                    // Handle error
                    android.widget.Toast.makeText(this, it.message, android.widget.Toast.LENGTH_LONG).show()
                }
                is LoadResult.Loading -> {
                    // Show loading indicator if needed
                }
            }
        }
        loadData(null)

        // Record current theme and font to detect changes
        currentThemeName = SettingsManager.getTheme(this)
        currentFontName = SettingsManager.getFont(this)

        // Expand/Collapse toggle button
        expandToggleButton.setOnClickListener {
            isExpandedMode = !isExpandedMode
            Log.d(TAG, "Expand toggle clicked: isExpandedMode=$isExpandedMode")
            SettingsManager.setExpandModeEnabled(this, isExpandedMode)
            pageAdapter.setExpandedMode(isExpandedMode)
            // Update icon appearance
            expandToggleButton.alpha = if (isExpandedMode) 1.0f else 0.6f
            (viewPager.getChildAt(0) as androidx.recyclerview.widget.RecyclerView).isNestedScrollingEnabled = !isExpandedMode
        }

        // Settings button
        settingsButton.setOnClickListener {
            showSettingsScreen()
        }

        // Navigation button listeners
        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        forwardButton.setOnClickListener {
            if (viewPager.currentItem < pageAdapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }

        homeButton.setOnClickListener {
            viewPager.currentItem = 0  // Home is always position 0
        }

        // Keypad button
        keypadButton.setOnClickListener {
            showKeypadDialog()
        }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem != 0) {
                    viewPager.currentItem = 0  // Go to home
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun loadData(lastUpdated: String?) {
        DataManager.pages?.let { jsonString ->
            try {
                val allPages = parser.parse(jsonString)
                pageAdapter.setPages(allPages, lastUpdated)
                updateNavigationBar(0)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle parsing error, maybe show a toast
                android.widget.Toast.makeText(this, "Error parsing teletext data: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        } ?: run {
            // DataManager.pages is null, this shouldn't happen if SplashActivity works correctly
            // but as a fallback, we can log an error or show a message.
            println("Error: DataManager.pages is null in MainActivity.loadData")
            android.widget.Toast.makeText(this, "Error: Teletext data not available.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun updateNavigationBar(position: Int) {
        val pageNum = pageAdapter.getPageNumberAt(position)
        currentPageNumber.text = "$pageNum"

        // Update button states
        backButton.isEnabled = position > 0
        backButton.alpha = if (position > 0) 1.0f else 0.3f

        forwardButton.isEnabled = position < pageAdapter.itemCount - 1
        forwardButton.alpha = if (position < pageAdapter.itemCount - 1) 1.0f else 0.3f

        homeButton.isEnabled = position != 0
        homeButton.alpha = if (position != 0) 1.0f else 0.3f
    }

    private fun navigateToPage(pageNumber: Int) {
        val position = pageAdapter.getPositionForPageNumber(pageNumber)
        viewPager.setCurrentItem(position, true)
    }

    private fun showKeypadDialog() {
        val dialog = KeypadDialog(this) { pageNumber ->
            navigateToPage(pageNumber)
        }
        dialog.show()
    }

    private fun showSettingsScreen() {
        val intent = android.content.Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openUrlInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no browser is available or URL is malformed
            android.widget.Toast.makeText(this, "Unable to open URL: $url", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: isExpandedMode=$isExpandedMode")
        // Check if theme has changed while we were paused
        val currentTheme = SettingsManager.getTheme(this)
        if (currentTheme != currentThemeName) {
            currentThemeName = currentTheme
            // Theme changed, refresh the adapter to apply new theme
            pageAdapter.notifyDataSetChanged()
        }
        
        // Check if font has changed
        val currentFont = SettingsManager.getFont(this)
        if (currentFont != currentFontName) {
            currentFontName = currentFont
            // Font changed, refresh the adapter to apply new font
            pageAdapter.notifyDataSetChanged()
        }

        // Check if toolbar position has changed
        val toolbarPosition = SettingsManager.getToolbarPosition(this)
        if (toolbarPosition != currentToolbarPosition) {
            currentToolbarPosition = toolbarPosition
            // Toolbar position changed, recreate the activity to apply new layout
            recreate()
        }
    }

    private fun applyToolbarPosition() {
        val navigationBar = findViewById<View>(R.id.navigationBar)
        val viewPagerView = findViewById<ViewPager2>(R.id.viewPager)
        val coordinateOverlay = findViewById<View>(R.id.coordinateOverlay)

        val params = navigationBar.layoutParams as ConstraintLayout.LayoutParams

        if (currentToolbarPosition == "top") {
            // Navigation bar at top
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
        } else {
            // Navigation bar at bottom (default)
            params.topToTop = ConstraintLayout.LayoutParams.UNSET
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToTop = R.id.coordinateOverlay
        }
        navigationBar.layoutParams = params

        // Update ViewPager constraints
        val viewPagerParams = viewPagerView.layoutParams as ConstraintLayout.LayoutParams
        if (currentToolbarPosition == "top") {
            viewPagerParams.topToTop = ConstraintLayout.LayoutParams.UNSET
            viewPagerParams.topToBottom = R.id.navigationBar
            viewPagerParams.bottomToTop = R.id.coordinateOverlay
        } else {
            viewPagerParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            viewPagerParams.topToBottom = ConstraintLayout.LayoutParams.UNSET
            viewPagerParams.bottomToTop = R.id.navigationBar
        }
        viewPagerView.layoutParams = viewPagerParams

        // Update coordinate overlay constraints
        val overlayParams = coordinateOverlay.layoutParams as ConstraintLayout.LayoutParams
        if (currentToolbarPosition == "top") {
            overlayParams.topToTop = ConstraintLayout.LayoutParams.UNSET
            overlayParams.topToBottom = ConstraintLayout.LayoutParams.UNSET
            overlayParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            overlayParams.topToTop = ConstraintLayout.LayoutParams.UNSET
            overlayParams.topToBottom = ConstraintLayout.LayoutParams.UNSET
            overlayParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }
        coordinateOverlay.layoutParams = overlayParams
    }
}