package com.kdr.nuven

import android.content.Context
import com.kdr.nuven.themes.*

object ThemeManager {

    private val themes = mapOf(
        "Teletext" to TeletextTheme.colors,
        "Raven" to RavenTheme.colors
    )

    fun getTheme(name: String): ThemeColors {
        return themes[name] ?: TeletextTheme.colors
    }

    fun getCurrentTheme(context: Context): ThemeColors {
        val themeName = SettingsManager.getTheme(context)
        return getTheme(themeName)
    }

    fun getColor(context: Context, colorRole: ThemeColor): Int {
        return getCurrentTheme(context).getColor(colorRole)
    }
}
