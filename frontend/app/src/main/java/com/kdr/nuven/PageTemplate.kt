package com.kdr.nuven

import android.content.Context
import com.kdr.nuven.themes.ThemeColor

object PageTemplate {
    
    fun createMainPageTemplate(): PageStyles {
        return PageStyles(
            backgroundColor = ThemeColor.MAIN_PAGE_BG,
            defaultTextColor = ThemeColor.MAIN_PAGE_FG,
            headerColor = ThemeColor.HEADER_FG,
            titleColor = ThemeColor.TITLE_TEXT,
            bodyColor = ThemeColor.BODY_TEXT
        )
    }
    
    fun createArticlePageTemplate(category: String = "default"): PageStyles {
        return PageStyles(
            backgroundColor = ThemeColor.ARTICLE_PAGE_BG,
            defaultTextColor = ThemeColor.ARTICLE_PAGE_FG,
            headerColor = ThemeColor.HEADER_FG,
            headerBackgroundColor = ThemeColor.CATEGORY_HEADER,
            statusColor = ThemeColor.STATUS_BAR_FG,
            statusBackgroundColor = ThemeColor.STATUS_BAR_BG,
            titleColor = ThemeColor.TITLE_TEXT,
            bodyColor = ThemeColor.BODY_TEXT
        )
    }
}

data class PageStyles(
    val backgroundColor: ThemeColor,
    val defaultTextColor: ThemeColor,
    val headerColor: ThemeColor,
    val headerBackgroundColor: ThemeColor? = null,
    val statusColor: ThemeColor? = null,
    val statusBackgroundColor: ThemeColor? = null,
    val titleColor: ThemeColor,
    val bodyColor: ThemeColor
)
