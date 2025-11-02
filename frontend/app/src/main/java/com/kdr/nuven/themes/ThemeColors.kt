package com.kdr.nuven.themes

import android.graphics.Color

data class ThemeColors(
    val name: String,
    private val colorMap: Map<ThemeColor, Int>
) {
    fun getColor(themeColor: ThemeColor): Int {
        return colorMap[themeColor] ?: Color.WHITE
    }
    
    fun hasColor(themeColor: ThemeColor): Boolean {
        return colorMap.containsKey(themeColor)
    }
}
