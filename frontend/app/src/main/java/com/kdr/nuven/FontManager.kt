package com.kdr.nuven

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

object FontManager {
    
    fun getTypeface(context: Context, fontName: String): Typeface {
        return when (fontName) {
            "JetBrains Mono" -> {
                ResourcesCompat.getFont(context, R.font.jetbrains_mono_regular) ?: Typeface.MONOSPACE
            }
            "VT323 (Retro)" -> {
                ResourcesCompat.getFont(context, R.font.vt323_regular) ?: Typeface.MONOSPACE
            }
            else -> Typeface.MONOSPACE // "System Monospace" or fallback
        }
    }
}
