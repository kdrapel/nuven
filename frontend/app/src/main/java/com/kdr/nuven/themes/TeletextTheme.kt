package com.kdr.nuven.themes

import android.graphics.Color

object TeletextTheme {
    val colors = ThemeColors(
        name = "Teletext",
        colorMap = mapOf(
            // Main page colors
            ThemeColor.MAIN_PAGE_BG to Color.parseColor("#006400"),  // Dark green
            ThemeColor.MAIN_PAGE_FG to Color.parseColor("#FFFFFF"),
            
            // Article page colors
            ThemeColor.ARTICLE_PAGE_BG to Color.parseColor("#000000"),
            ThemeColor.ARTICLE_PAGE_FG to Color.parseColor("#FFFFFF"),
            
            // Text colors
            ThemeColor.TITLE_TEXT to Color.parseColor("#FFFF00"),
            ThemeColor.BODY_TEXT to Color.parseColor("#00FFFF"),
            ThemeColor.BODY_TEXT_ALT to Color.parseColor("#FFFFFF"),
            ThemeColor.HEADER_FG to Color.parseColor("#FFFFFF"),
            ThemeColor.HEADER_BG to Color.parseColor("#006400"),
            
            // Sections
            ThemeColor.TOP_ARTICLES_BG to Color.parseColor("#FFFFFF"),
            ThemeColor.TOP_ARTICLES_FG to Color.parseColor("#0000FF"),
            ThemeColor.LATEST_NEWS_HEADER_FG to Color.parseColor("#FFFFFF"),
            ThemeColor.LATEST_NEWS_HEADER_BG to Color.parseColor("#008000"),
            ThemeColor.LATEST_NEWS_BG to Color.parseColor("#FFFFFF"),
            ThemeColor.LATEST_NEWS_FG to Color.parseColor("#0000FF"),
            
            // Index page
            ThemeColor.INDEX_HEADER_FG to Color.parseColor("#0000FF"),
            ThemeColor.INDEX_HEADER_BG to Color.parseColor("#FFFFFF"),
            ThemeColor.INDEX_ITEM_FG to Color.parseColor("#0000FF"),
            ThemeColor.INDEX_ITEM_BG to Color.parseColor("#FFFFFF"),
            
            // Margins and padding
            ThemeColor.MARGIN_FG to Color.parseColor("#000000"),
            ThemeColor.MARGIN_BG to Color.parseColor("#FFFFFF"),
            
            // Navigation and interactive elements
            ThemeColor.NAVIGATION_BG to Color.parseColor("#000000"),
            ThemeColor.NAVIGATION_DISABLED_FG to Color.parseColor("#808080"),
            
            // Special elements
            ThemeColor.PAGE_NUMBERS_FG to Color.parseColor("#FF0000"),
            ThemeColor.STATUS_BAR_BG to Color.parseColor("#FFFFFF"),
            ThemeColor.STATUS_BAR_FG to Color.parseColor("#000000"),
            
            // Category headers
            ThemeColor.CATEGORY_HEADER to Color.parseColor("#FF0000"),
            
            // Misc
            ThemeColor.LINK_COLOR to Color.parseColor("#FF0000"),
            ThemeColor.DATE_TIME_FG to Color.parseColor("#008000"),
            ThemeColor.DATE_TIME_BG to Color.parseColor("#008000")
        )
    )
}
