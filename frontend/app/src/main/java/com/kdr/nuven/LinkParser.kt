package com.kdr.nuven

/**
 * Handles parsing and extraction of page links from content
 */
class LinkParser {

    private val linkPattern = Regex("""\{\{(\d+)\}\}""")
    private val urlPattern = Regex("""\{\{URL:(.+)\}\}""")
    
    /**
     * Extract page number from a line containing {{pageNumber}}
     */
    fun extractPageNumber(line: String): Int? {
        val match = linkPattern.find(line)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Extract URL from a line containing {{URL:text}}
     */
    fun extractUrl(line: String): String? {
        val match = urlPattern.find(line)
        return match?.groupValues?.get(1)
    }
    
    /**
     * Remove link markers from text for display
     * "Text... {{105}}" becomes "Text..."
     */
    fun removeLinkMarkers(text: String): String {
        return linkPattern.replace(text, "")
    }
    
    /**
     * Check if a line contains a link
     */
    fun hasLink(line: String): Boolean {
        return linkPattern.containsMatchIn(line) || urlPattern.containsMatchIn(line)
    }
    
    /**
     * Get all links from a list of lines with their positions
     */
    fun extractLinks(lines: List<String>): List<LinkInfo> {
        return lines.mapIndexedNotNull { index, line ->
            extractPageNumber(line)?.let { pageNumber ->
                LinkInfo(
                    lineIndex = index,
                    pageNumber = pageNumber,
                    url = null,
                    originalText = line,
                    displayText = removeLinkMarkers(line)
                )
            } ?: extractUrl(line)?.let { url ->
                LinkInfo(
                    lineIndex = index,
                    pageNumber = null,
                    url = url,
                    originalText = line,
                    displayText = urlPattern.replace(line, "$1")
                )
            }
        }
    }

    data class LinkInfo(
        val lineIndex: Int,
        val pageNumber: Int?,
        val url: String?,
        val originalText: String,
        val displayText: String
    )
}
