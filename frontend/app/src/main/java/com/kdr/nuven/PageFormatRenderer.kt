package com.kdr.nuven

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import com.kdr.nuven.themes.ThemeColor
import com.kdr.nuven.themes.ColorCodeMapper
import com.kdr.nuven.themes.ThemeColors

class PageFormatRenderer {
    
    companion object {
        private const val TAG = "PageFormatRenderer"
    }
    
    private val linkParser = LinkParser()
    private var isExpandedMode = false
    private var isDebugMode = false
    
    fun setExpandedMode(expanded: Boolean) {
        Log.d(TAG, "setExpandedMode called: $expanded (previous: $isExpandedMode)")
        isExpandedMode = expanded
    }

    fun setDebugMode(debug: Boolean) {
        isDebugMode = debug
    }
    
    fun renderPage(
        pageFormat: PageFormat,
        textView: TextView,
        lineNumberTextView: TextView,
        onLinkClick: ((Int) -> Unit)? = null,
        onUrlClick: ((String) -> Unit)? = null,
        onCoordinateUpdate: ((Int, Int) -> Unit)? = null,
        lastUpdated: String? = null
    ) {
        Log.d(TAG, "renderPage: pageId=${pageFormat.pageId}, isExpandedMode=$isExpandedMode")
        val template = if (pageFormat.metadata.category == "main") {
            PageTemplate.createMainPageTemplate()
        } else {
            PageTemplate.createArticlePageTemplate(pageFormat.metadata.category)
        }
        
        val theme = ThemeManager.getCurrentTheme(textView.context)
        
        textView.setBackgroundColor(theme.getColor(template.backgroundColor))
        textView.setTextColor(theme.getColor(template.defaultTextColor))
        
        // Apply selected font
        val selectedFont = SettingsManager.getFont(textView.context)
        textView.typeface = FontManager.getTypeface(textView.context, selectedFont)
        
        // Calculate optimal text size based on available width and height
        calculateAndSetTextSize(textView, lineNumberTextView, pageFormat.globalStyles.maxPageWidth, pageFormat.globalStyles.maxPageHeight)
        
        // Enable link clicking for page links only
        if (onLinkClick != null) {
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
        
        val builder = SpannableStringBuilder()
        val maxWidth = pageFormat.globalStyles.maxPageWidth
        
        // Store line-to-section mapping for link handling
        val lineLinks = mutableMapOf<Int, LinkParser.LinkInfo>()
        
        // Create a 2D grid to position sections
        val grid = Array(pageFormat.globalStyles.maxPageHeight) { 
            CharArray(maxWidth) { ' ' } 
        }
        val colorGrid = Array(pageFormat.globalStyles.maxPageHeight) { 
            Array<ColorInfo?>(maxWidth) { null } 
        }
        
        // Place all sections in the grid
        pageFormat.sections.forEach { section ->
            placeSectionInGrid(section, grid, colorGrid, maxWidth, lineLinks)
        }

        if (pageFormat.pageId == "100" && lastUpdated != null) {
            // Format the timestamp like article publication dates: "yyyy/MM/dd HH:mm"
            var formattedLastUpdated = lastUpdated
            try {
                // Parse ISO format timestamp from GitHub API
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(lastUpdated)
                
                if (date != null) {
                    val outputFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
                    formattedLastUpdated = outputFormat.format(date)
                }
            } catch (e: Exception) {
                // If parsing fails, use the original value
                e.printStackTrace()
            }
            
            val line = " Last updated: $formattedLastUpdated"
            val row = 5 // Line 6 (0-indexed)
            
            // Use theme colors for the last updated text
            val lastUpdatedFg = theme.getColor(ThemeColor.MAIN_PAGE_FG)
            val lastUpdatedBg = theme.getColor(ThemeColor.MAIN_PAGE_BG)
            
            line.forEachIndexed { index, char ->
                if (index < grid[row].size) {
                    grid[row][index] = char
                    colorGrid[row][index] = ColorInfo(
                        ColorCodeMapper.toCode(ThemeColor.MAIN_PAGE_FG),
                        ColorCodeMapper.toCode(ThemeColor.MAIN_PAGE_BG)
                    )
                }
            }
        }

        val pageId = pageFormat.pageId.toIntOrNull()
        if (pageId != null && pageId in 101..109) {
            val marginBgColorCode = ColorCodeMapper.toCode(ThemeColor.MARGIN_BG)
            
            // Add left margin
            for (row in grid.indices) {
                for (col in grid[row].size - 1 downTo 1) {
                    grid[row][col] = grid[row][col - 1]
                    colorGrid[row][col] = colorGrid[row][col - 1]
                }
                grid[row][0] = ' '
                colorGrid[row][0] = ColorInfo(ColorCodeMapper.toCode(ThemeColor.MARGIN_FG), marginBgColorCode)
            }
            
            // Add right margin
            for (row in grid.indices) {
                for (col in 0 until grid[row].size - 1) {
                    grid[row][col] = grid[row][col + 1]
                    colorGrid[row][col] = colorGrid[row][col + 1]
                }
                grid[row][grid[row].size - 1] = ' '
                colorGrid[row][grid[row].size - 1] = ColorInfo(ColorCodeMapper.toCode(ThemeColor.MARGIN_FG), marginBgColorCode)
            }
        }
        
        val defaultBackgroundColor = resolveColor(pageFormat.globalStyles.defaultBackground, theme)

        // Convert grid to SpannableString
        grid.forEachIndexed { rowIndex, row ->
            // Always use full width - pad to maxWidth
            val rowText = String(row).padEnd(maxWidth)
            val start = builder.length
            builder.append(rowText)
            val end = builder.length
            
            // Check if this row has page links for page number coloring
            val hasPageLink = lineLinks[rowIndex] != null && lineLinks[rowIndex]?.pageNumber != null
            
            // Apply colors for this row - ensure full row has proper background
            for (col in 0 until maxWidth) {
                val colorInfo = colorGrid[rowIndex][col]
                if (colorInfo != null) {
                    val finalForegroundColor = resolveColor(colorInfo.foreground, theme)
                    val mappedBackgroundColor = colorInfo.background?.let { resolveColor(it, theme) }
                    
                    val spanStart = start + col
                    val spanEnd = spanStart + 1
                    if (spanEnd <= end) {
                        builder.setSpan(
                            ForegroundColorSpan(finalForegroundColor),
                            spanStart,
                            spanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        if (mappedBackgroundColor != null) {
                            builder.setSpan(
                                BackgroundColorSpan(mappedBackgroundColor),
                                spanStart,
                                spanEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                } else {
                    // No color info - apply default
                    val spanStart = start + col
                    val spanEnd = spanStart + 1
                    if (spanEnd <= end) {
                        builder.setSpan(
                            BackgroundColorSpan(defaultBackgroundColor),
                            spanStart,
                            spanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            
            // Add clickable span for page numbers only (not entire line)
            if (lineLinks.containsKey(rowIndex)) {
                val linkInfo = lineLinks[rowIndex]!!
                if (linkInfo.pageNumber != null && onLinkClick != null) {
                    // Find the page number position in the row
                    val pageNumStr = linkInfo.pageNumber.toString()
                    val rowText = String(grid[rowIndex])
                    val pageNumIndex = rowText.lastIndexOf(pageNumStr)
                    
                    if (pageNumIndex >= 0) {
                        // Only make the page number clickable, not the entire line
                        val clickableStart = start + pageNumIndex
                        val clickableEnd = clickableStart + pageNumStr.length
                        
                        if (clickableEnd <= end) {
                            builder.setSpan(
                                object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        onLinkClick(linkInfo.pageNumber!!)
                                    }

                                    override fun updateDrawState(ds: android.text.TextPaint) {
                                        // Don't change text appearance (no underline)
                                        ds.isUnderlineText = false
                                    }
                                },
                                clickableStart,
                                clickableEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                // URL links removed - no longer clickable
            }


            
            // Only add newline if not the last row
            if (rowIndex < grid.size - 1) {
                builder.append("\n")
            }
        }
        
        textView.text = builder

        // Add touch and mouse listener for coordinate calculation
        if (onCoordinateUpdate != null && isDebugMode) {
            textView.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_MOVE,
                    android.view.MotionEvent.ACTION_HOVER_MOVE -> {
                        val x = event.x.toInt()
                        val y = event.y.toInt()

                        // Calculate character size (approximately)
                        val paint = textView.paint
                        val charWidth = paint.measureText("M")
                        val fontMetrics = paint.fontMetrics
                        val charHeight = fontMetrics.descent - fontMetrics.ascent

                        // Calculate teletext coordinates based on character grid (40x25)
                        val teletextX = (x / charWidth).toInt().coerceIn(0, 39)
                        val teletextY = (y / charHeight).toInt().coerceIn(0, 24)

                        onCoordinateUpdate(teletextX, teletextY)
                    }
                }
                false // Return false to allow ClickableSpan to work
            }
        } else {
            textView.setOnTouchListener(null)
        }

        if (isDebugMode) {
            val lineNumbers = (1..pageFormat.globalStyles.maxPageHeight).joinToString("\n")
            lineNumberTextView.text = lineNumbers
            lineNumberTextView.visibility = View.VISIBLE
        } else {
            lineNumberTextView.text = ""
            lineNumberTextView.visibility = View.GONE
        }
    }
    
    /**
     * Calculate and set text size to make content fit screen optimally
     * In normal mode: fit 40 chars width
     * In expanded mode: fit 40 chars width AND stretch to fill height evenly
     */
    private fun calculateAndSetTextSize(textView: TextView, lineNumberTextView: TextView, targetChars: Int, targetLines: Int) {
        Log.d(TAG, "calculateAndSetTextSize: isExpandedMode=$isExpandedMode, targetChars=$targetChars, targetLines=$targetLines, viewWidth=${textView.width}, viewHeight=${textView.height}")
        
        // Use post to defer until after layout pass completes
        textView.post {
            val width = textView.width
            val height = textView.height
            Log.d(TAG, "post executed: width=$width, height=$height")
            
            if (width > 0 && height > 0) {
                performTextSizeCalculation(textView, lineNumberTextView, targetChars, targetLines)
            } else {
                // Fallback: wait for layout if dimensions still not available
                Log.w(TAG, "Dimensions still not available after post, using ViewTreeObserver")
                textView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        Log.d(TAG, "onGlobalLayout triggered, about to calculate")
                        performTextSizeCalculation(textView, lineNumberTextView, targetChars, targetLines)
                    }
                })
            }
        }
    }
    
    private fun performTextSizeCalculation(textView: TextView, lineNumberTextView: TextView, targetChars: Int, targetLines: Int) {
        val availableWidth = textView.width - textView.paddingLeft - textView.paddingRight
        val availableHeight = textView.height - textView.paddingTop - textView.paddingBottom
        
        Log.d(TAG, "performTextSizeCalculation: isExpandedMode=$isExpandedMode, availableWidth=$availableWidth, availableHeight=$availableHeight")
        Log.d(TAG, "TextView actual dimensions: width=${textView.width}, height=${textView.height}, padding: L=${textView.paddingLeft} R=${textView.paddingRight} T=${textView.paddingTop} B=${textView.paddingBottom}")
        
        if (availableWidth > 0 && availableHeight > 0) {
            val paint = textView.paint
            
            // Calculate text size to fill the full available width
            val targetCharWidth = availableWidth / targetChars.toFloat()
            
            Log.d(TAG, "Target: $targetChars chars should fit in ${availableWidth}px, targetCharWidth=${targetCharWidth}px")

            // Binary search for text size that gives the target character width
            var low = 8f
            var high = 30f

            while (high - low > 0.5f) {
                val textSize = (low + high) / 2
                paint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSize,
                    textView.context.resources.displayMetrics
                )

                val charWidth = paint.measureText("M")

                if (charWidth <= targetCharWidth) {
                    low = textSize
                } else {
                    high = textSize
                }
            }
            
            val finalCharWidth = paint.measureText("M")
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, low)
            lineNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, low)
            
            Log.d(TAG, "Initial text size calculated: ${low}sp, actual char width: ${finalCharWidth}px")
            
            // In expanded mode, adjust to fill screen height without causing scrolling
            if (isExpandedMode) {
                Log.d(TAG, "Applying expanded mode adjustments")
                // Get the current font metrics after setting text size
                val fontMetrics = paint.fontMetrics
                val currentLineHeight = fontMetrics.descent - fontMetrics.ascent
                val totalContentHeight = currentLineHeight * targetLines
            
                if (totalContentHeight > 0) {
                    // Calculate exact text size to fit content in available height
                    val requiredTextSize = if (totalContentHeight > availableHeight) {
                        // Reduce text size to fit
                        low * (availableHeight / totalContentHeight) * 0.98f // Small buffer to ensure it fits
                    } else {
                        // Keep current size but adjust line spacing
                        low
                    }
                    
                    // Apply the text size
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, requiredTextSize)
                    lineNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, requiredTextSize)
                    
                    // Update paint to get new font metrics
                    paint.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        requiredTextSize,
                        textView.context.resources.displayMetrics
                    )
                    val newFontMetrics = paint.fontMetrics
                    val newLineHeight = newFontMetrics.descent - newFontMetrics.ascent
                    val newTotalHeight = newLineHeight * targetLines
                    
                    // Calculate line spacing to fill exactly the available height without exceeding it
                    var lineSpacingExtra = 0f
                    if (targetLines > 1) {
                        val spaceToDistribute = (availableHeight - newTotalHeight) / (targetLines - 1)
                        // Ensure we don't add spacing if it would cause scrolling
                        lineSpacingExtra = if (spaceToDistribute > 0.5f) spaceToDistribute - 0.5f else 0f
                    }
                    
                    textView.setLineSpacing(lineSpacingExtra, 1.0f)
                    lineNumberTextView.setLineSpacing(lineSpacingExtra, 1.0f)
                    Log.d(TAG, "Final text size: ${requiredTextSize}sp, lineSpacing: $lineSpacingExtra")
                }

                // Ensure scaleY is reset to avoid scaling issues
                textView.scaleY = 1.0f
                lineNumberTextView.scaleY = 1.0f
                
                // Force re-layout to apply changes immediately
                textView.requestLayout()
                lineNumberTextView.requestLayout()
            } else {
                Log.d(TAG, "Normal mode - no adjustments, final text size: ${low}sp")
                // Normal mode - no extra line spacing
                textView.scaleY = 1.0f
                textView.setLineSpacing(0f, 1.0f)
                lineNumberTextView.scaleY = 1.0f
                lineNumberTextView.setLineSpacing(0f, 1.0f)
                
                // Force re-layout to apply changes immediately
                textView.requestLayout()
                lineNumberTextView.requestLayout()
            }
        } else {
            Log.w(TAG, "Cannot calculate text size: availableWidth=$availableWidth, availableHeight=$availableHeight")
        }
    }
    
    private fun resolveColor(colorCode: String, theme: ThemeColors): Int {
        val themeColor = ColorCodeMapper.fromCode(colorCode)
        return if (themeColor != null) {
            theme.getColor(themeColor)
        } else {
            try {
                Color.parseColor(colorCode)
            } catch (e: Exception) {
                Color.WHITE
            }
        }
    }
    
    private fun placeSectionInGrid(
        section: Section,
        grid: Array<CharArray>,
        colorGrid: Array<Array<ColorInfo?>>,
        maxWidth: Int,
        lineLinks: MutableMap<Int, LinkParser.LinkInfo>
    ) {
        val startRow = section.position.row
        val startCol = section.position.column

        val foregroundColor = section.styles.foreground
        val backgroundColor = section.styles.background

        val content = when (val c = section.content) {
            is SectionContent.Text -> listOf(c.value)
            is SectionContent.Lines -> c.value
        }

        content.forEachIndexed { lineIndex, line ->
            val row = startRow + lineIndex
            if (row >= grid.size) return@forEachIndexed

            // Extract page number only (URL links removed)
            val pageNumber = linkParser.extractPageNumber(line)

            if (pageNumber != null) {
                lineLinks[row] = LinkParser.LinkInfo(
                    lineIndex = row,
                    pageNumber = pageNumber,
                    url = null,
                    originalText = line,
                    displayText = linkParser.removeLinkMarkers(line)
                )
            }

            // Remove {{}} markers but keep the content visible
            var displayLine = line
            val linkMatch = Regex("""\{\{(\d+)\}\}""").find(line)
            val urlMatch = Regex("""\{\{URL:(.+)\}\}""").find(line)

            var pageNumPart = ""
            if (linkMatch != null) {
                val pageNumStr = linkMatch.groupValues[1]
                displayLine = line.replace(linkMatch.value, pageNumStr)
                pageNumPart = pageNumStr
            } else if (urlMatch != null) {
                // Just remove the URL marker, display the text normally (not clickable)
                val urlText = urlMatch.groupValues[1]
                displayLine = line.replace(urlMatch.value, urlText)
            }

            val effectiveWidth = section.styles.maxWidth ?: maxWidth
            val paddedLine = when (section.styles.textAlign) {
                "center" -> displayLine.padCenter(effectiveWidth)
                "right" -> displayLine.padStart(effectiveWidth)
                else -> displayLine.padEnd(effectiveWidth)
            }

            // Find where the page number starts
            val pageNumStartCol = if (pageNumPart.isNotEmpty()) paddedLine.lastIndexOf(pageNumPart) else -1

            paddedLine.take(effectiveWidth).forEachIndexed { charIndex, char ->
                val col = startCol + charIndex
                if (col < grid[row].size) {
                    grid[row][col] = char

                    // Use red color for page numbers (will be themed later in the rendering loop)
                    val isPageNumChar = pageNumStartCol != -1 && charIndex >= pageNumStartCol && charIndex < pageNumStartCol + pageNumPart.length
                    val charColor = if (isPageNumChar) {
                        ColorInfo(ColorCodeMapper.toCode(ThemeColor.PAGE_NUMBERS_FG), backgroundColor)
                    } else {
                        ColorInfo(foregroundColor, backgroundColor)
                    }

                    colorGrid[row][col] = charColor
                }
            }
        }
    }
    
    private fun String.padCenter(width: Int): String {
        if (this.length >= width) return this.take(width)
        val totalPadding = width - this.length
        val leftPadding = totalPadding / 2
        val rightPadding = totalPadding - leftPadding
        return " ".repeat(leftPadding) + this + " ".repeat(rightPadding)
    }
    
    private data class ColorInfo(val foreground: String, val background: String?)
}
