package com.kdr.nuven.themes

object ColorCodeMapper {
    private const val CODE_MAIN_PAGE_BG = "MAIN_PAGE_BG"
    private const val CODE_MAIN_PAGE_FG = "MAIN_PAGE_FG"
    private const val CODE_ARTICLE_PAGE_BG = "ARTICLE_PAGE_BG"
    private const val CODE_ARTICLE_PAGE_FG = "ARTICLE_PAGE_FG"
    private const val CODE_TITLE_TEXT = "TITLE_TEXT"
    private const val CODE_BODY_TEXT = "BODY_TEXT"
    private const val CODE_BODY_TEXT_ALT = "BODY_TEXT_ALT"
    private const val CODE_HEADER_FG = "HEADER_FG"
    private const val CODE_HEADER_BG = "HEADER_BG"
    private const val CODE_TOP_ARTICLES_BG = "TOP_ARTICLES_BG"
    private const val CODE_TOP_ARTICLES_FG = "TOP_ARTICLES_FG"
    private const val CODE_LATEST_NEWS_HEADER_FG = "LATEST_NEWS_HEADER_FG"
    private const val CODE_LATEST_NEWS_HEADER_BG = "LATEST_NEWS_HEADER_BG"
    private const val CODE_LATEST_NEWS_BG = "LATEST_NEWS_BG"
    private const val CODE_LATEST_NEWS_FG = "LATEST_NEWS_FG"
    private const val CODE_INDEX_HEADER_FG = "INDEX_HEADER_FG"
    private const val CODE_INDEX_HEADER_BG = "INDEX_HEADER_BG"
    private const val CODE_INDEX_ITEM_FG = "INDEX_ITEM_FG"
    private const val CODE_INDEX_ITEM_BG = "INDEX_ITEM_BG"
    private const val CODE_MARGIN_FG = "MARGIN_FG"
    private const val CODE_MARGIN_BG = "MARGIN_BG"
    private const val CODE_NAVIGATION_BG = "NAVIGATION_BG"
    private const val CODE_NAVIGATION_DISABLED_FG = "NAVIGATION_DISABLED_FG"
    private const val CODE_PAGE_NUMBERS_FG = "PAGE_NUMBERS_FG"
    private const val CODE_STATUS_BAR_BG = "STATUS_BAR_BG"
    private const val CODE_STATUS_BAR_FG = "STATUS_BAR_FG"
    private const val CODE_CATEGORY_HEADER = "CATEGORY_HEADER"
    private const val CODE_LINK_COLOR = "LINK_COLOR"
    private const val CODE_DATE_TIME_FG = "DATE_TIME_FG"
    private const val CODE_DATE_TIME_BG = "DATE_TIME_BG"

    fun toCode(themeColor: ThemeColor): String {
        return when (themeColor) {
            ThemeColor.MAIN_PAGE_BG -> CODE_MAIN_PAGE_BG
            ThemeColor.MAIN_PAGE_FG -> CODE_MAIN_PAGE_FG
            ThemeColor.ARTICLE_PAGE_BG -> CODE_ARTICLE_PAGE_BG
            ThemeColor.ARTICLE_PAGE_FG -> CODE_ARTICLE_PAGE_FG
            ThemeColor.TITLE_TEXT -> CODE_TITLE_TEXT
            ThemeColor.BODY_TEXT -> CODE_BODY_TEXT
            ThemeColor.BODY_TEXT_ALT -> CODE_BODY_TEXT_ALT
            ThemeColor.HEADER_FG -> CODE_HEADER_FG
            ThemeColor.HEADER_BG -> CODE_HEADER_BG
            ThemeColor.TOP_ARTICLES_BG -> CODE_TOP_ARTICLES_BG
            ThemeColor.TOP_ARTICLES_FG -> CODE_TOP_ARTICLES_FG
            ThemeColor.LATEST_NEWS_HEADER_FG -> CODE_LATEST_NEWS_HEADER_FG
            ThemeColor.LATEST_NEWS_HEADER_BG -> CODE_LATEST_NEWS_HEADER_BG
            ThemeColor.LATEST_NEWS_BG -> CODE_LATEST_NEWS_BG
            ThemeColor.LATEST_NEWS_FG -> CODE_LATEST_NEWS_FG
            ThemeColor.INDEX_HEADER_FG -> CODE_INDEX_HEADER_FG
            ThemeColor.INDEX_HEADER_BG -> CODE_INDEX_HEADER_BG
            ThemeColor.INDEX_ITEM_FG -> CODE_INDEX_ITEM_FG
            ThemeColor.INDEX_ITEM_BG -> CODE_INDEX_ITEM_BG
            ThemeColor.MARGIN_FG -> CODE_MARGIN_FG
            ThemeColor.MARGIN_BG -> CODE_MARGIN_BG
            ThemeColor.NAVIGATION_BG -> CODE_NAVIGATION_BG
            ThemeColor.NAVIGATION_DISABLED_FG -> CODE_NAVIGATION_DISABLED_FG
            ThemeColor.PAGE_NUMBERS_FG -> CODE_PAGE_NUMBERS_FG
            ThemeColor.STATUS_BAR_BG -> CODE_STATUS_BAR_BG
            ThemeColor.STATUS_BAR_FG -> CODE_STATUS_BAR_FG
            ThemeColor.CATEGORY_HEADER -> CODE_CATEGORY_HEADER
            ThemeColor.LINK_COLOR -> CODE_LINK_COLOR
            ThemeColor.DATE_TIME_FG -> CODE_DATE_TIME_FG
            ThemeColor.DATE_TIME_BG -> CODE_DATE_TIME_BG
        }
    }

    fun fromCode(code: String): ThemeColor? {
        return when (code) {
            CODE_MAIN_PAGE_BG -> ThemeColor.MAIN_PAGE_BG
            CODE_MAIN_PAGE_FG -> ThemeColor.MAIN_PAGE_FG
            CODE_ARTICLE_PAGE_BG -> ThemeColor.ARTICLE_PAGE_BG
            CODE_ARTICLE_PAGE_FG -> ThemeColor.ARTICLE_PAGE_FG
            CODE_TITLE_TEXT -> ThemeColor.TITLE_TEXT
            CODE_BODY_TEXT -> ThemeColor.BODY_TEXT
            CODE_BODY_TEXT_ALT -> ThemeColor.BODY_TEXT_ALT
            CODE_HEADER_FG -> ThemeColor.HEADER_FG
            CODE_HEADER_BG -> ThemeColor.HEADER_BG
            CODE_TOP_ARTICLES_BG -> ThemeColor.TOP_ARTICLES_BG
            CODE_TOP_ARTICLES_FG -> ThemeColor.TOP_ARTICLES_FG
            CODE_LATEST_NEWS_HEADER_FG -> ThemeColor.LATEST_NEWS_HEADER_FG
            CODE_LATEST_NEWS_HEADER_BG -> ThemeColor.LATEST_NEWS_HEADER_BG
            CODE_LATEST_NEWS_BG -> ThemeColor.LATEST_NEWS_BG
            CODE_LATEST_NEWS_FG -> ThemeColor.LATEST_NEWS_FG
            CODE_INDEX_HEADER_FG -> ThemeColor.INDEX_HEADER_FG
            CODE_INDEX_HEADER_BG -> ThemeColor.INDEX_HEADER_BG
            CODE_INDEX_ITEM_FG -> ThemeColor.INDEX_ITEM_FG
            CODE_INDEX_ITEM_BG -> ThemeColor.INDEX_ITEM_BG
            CODE_MARGIN_FG -> ThemeColor.MARGIN_FG
            CODE_MARGIN_BG -> ThemeColor.MARGIN_BG
            CODE_NAVIGATION_BG -> ThemeColor.NAVIGATION_BG
            CODE_NAVIGATION_DISABLED_FG -> ThemeColor.NAVIGATION_DISABLED_FG
            CODE_PAGE_NUMBERS_FG -> ThemeColor.PAGE_NUMBERS_FG
            CODE_STATUS_BAR_BG -> ThemeColor.STATUS_BAR_BG
            CODE_STATUS_BAR_FG -> ThemeColor.STATUS_BAR_FG
            CODE_CATEGORY_HEADER -> ThemeColor.CATEGORY_HEADER
            CODE_LINK_COLOR -> ThemeColor.LINK_COLOR
            CODE_DATE_TIME_FG -> ThemeColor.DATE_TIME_FG
            CODE_DATE_TIME_BG -> ThemeColor.DATE_TIME_BG
            else -> null
        }
    }
}
