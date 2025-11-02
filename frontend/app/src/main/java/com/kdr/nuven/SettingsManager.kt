package com.kdr.nuven

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREFS_NAME = "TeletextSettings"
    private const val THEME_KEY = "theme"
    private const val EXPAND_MODE_KEY = "expand_mode"
    private const val TOOLBAR_POSITION_KEY = "toolbar_position"
    private const val FONT_KEY = "font"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTheme(context: Context): String {
        return getPrefs(context).getString(THEME_KEY, "Teletext") ?: "Teletext"
    }

    fun setTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(THEME_KEY, theme).apply()
    }

    fun isExpandModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(EXPAND_MODE_KEY, false)
    }

    fun setExpandModeEnabled(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(EXPAND_MODE_KEY, isEnabled).apply()
    }

    fun getToolbarPosition(context: Context): String {
        return getPrefs(context).getString(TOOLBAR_POSITION_KEY, "bottom") ?: "bottom"
    }

    fun setToolbarPosition(context: Context, position: String) {
        getPrefs(context).edit().putString(TOOLBAR_POSITION_KEY, position).apply()
    }
    
    fun getFont(context: Context): String {
        return getPrefs(context).getString(FONT_KEY, "System Monospace") ?: "System Monospace"
    }
    
    fun setFont(context: Context, font: String) {
        getPrefs(context).edit().putString(FONT_KEY, font).apply()
    }
}
