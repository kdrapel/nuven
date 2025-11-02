package com.kdr.nuven.themes

import android.graphics.Color

object RavenTheme {
    val colors = ThemeColors(
        name = "Raven",
        colorMap = mapOf(
            // Main page colors (dark green kept)
            ThemeColor.MAIN_PAGE_BG to Color.parseColor("#4545EF"),  // Darker blue
            ThemeColor.MAIN_PAGE_FG to Color.parseColor("#CCCCCC"),  // Light gray instead of white
            
            // Article page colors
            ThemeColor.ARTICLE_PAGE_BG to Color.parseColor("#000000"),  // Black background
            ThemeColor.ARTICLE_PAGE_FG to Color.parseColor("#CCCCCC"),  // Light gray instead of white
            
            // Text colors
            ThemeColor.TITLE_TEXT to Color.parseColor("#5555FF"),
            ThemeColor.BODY_TEXT to Color.parseColor("#CCCCCC"),
            ThemeColor.BODY_TEXT_ALT to Color.parseColor("#999999"),
            ThemeColor.HEADER_FG to Color.parseColor("#CCCCCC"),
            ThemeColor.HEADER_BG to Color.parseColor("#5555FF"),
            
            // Sections (white backgrounds become dark gray)
            ThemeColor.TOP_ARTICLES_BG to Color.parseColor("#1a1a1a"),  // Dark gray instead of white
            ThemeColor.TOP_ARTICLES_FG to Color.parseColor("#5555FF"),  // Brighter blue for visibility
            ThemeColor.LATEST_NEWS_HEADER_FG to Color.parseColor("#CCCCCC"),  // Light gray
            ThemeColor.LATEST_NEWS_HEADER_BG to Color.parseColor("#4545EF"),  // Keep green
            ThemeColor.LATEST_NEWS_BG to Color.parseColor("#1a1a1a"),  // Dark gray instead of white
            ThemeColor.LATEST_NEWS_FG to Color.parseColor("#5555FF"),  // Brighter blue
            
            // Index page (dark mode)
            ThemeColor.INDEX_HEADER_FG to Color.parseColor("#5555FF"),  // Brighter blue
            ThemeColor.INDEX_HEADER_BG to Color.parseColor("#1a1a1a"),  // Dark gray
            ThemeColor.INDEX_ITEM_FG to Color.parseColor("#5555FF"),   // Brighter blue
            ThemeColor.INDEX_ITEM_BG to Color.parseColor("#1a1a1a"),   // Dark gray
            
            // Margins and padding
            ThemeColor.MARGIN_FG to Color.parseColor("#000000"),
            ThemeColor.MARGIN_BG to Color.parseColor("#1a1a1a"),  // Dark gray
            
            // Navigation and interactive elements
            ThemeColor.NAVIGATION_BG to Color.parseColor("#000000"),
            ThemeColor.NAVIGATION_DISABLED_FG to Color.parseColor("#666666"),  // Medium gray
            
            // Special elements
            ThemeColor.PAGE_NUMBERS_FG to Color.parseColor("#8585FF"),  // Brighter red
            ThemeColor.STATUS_BAR_BG to Color.parseColor("#1a1a1a"),  // Dark gray
            ThemeColor.STATUS_BAR_FG to Color.parseColor("#CCCCCC"),  // Light gray
            
            // Category headers
            ThemeColor.CATEGORY_HEADER to Color.parseColor("#FF5555"),  // Brighter red
            
            // Misc
            ThemeColor.LINK_COLOR to Color.parseColor("#6565FF"),  // Brighter red
            ThemeColor.DATE_TIME_FG to Color.parseColor("#5555FF"),
            ThemeColor.DATE_TIME_BG to Color.parseColor("#0d0d0d")   // Very dark gray for date area
        )
    )
}
