package com.kdr.nuven

import android.content.Context
import com.kdr.nuven.themes.ThemeColor
import com.kdr.nuven.themes.ColorCodeMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class TeletextData(
    val articles: List<Article>
)

@Serializable
data class Article(
    val id: String,
    val topic: String,
    val shortTitle: String,
    val mediumTitle: String,
    val longTitle: String,
    val paragraphs: List<Paragraph>,
    val publishedAt: String,
    val ranking: Float? = null,
    val source: String? = null,
    @SerialName("articleUrl") val url: String? = null
)

@Serializable
data class Paragraph(
    @SerialName("lines") private val lowerCaseLines: List<String>? = null,
    @SerialName("Lines") private val upperCaseLines: List<String>? = null
) {
    val lines: List<String>
        get() = lowerCaseLines ?: upperCaseLines ?: emptyList()
}

class TeletextParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun color(themeColor: ThemeColor): String = ColorCodeMapper.toCode(themeColor)

    fun parse(jsonString: String): List<PageFormat> {
        val teletextData = json.decodeFromString<TeletextData>(jsonString)

        val allValidArticles = teletextData.articles
            .filter { it.paragraphs.isNotEmpty() && it.paragraphs.any { p -> p.lines.isNotEmpty() } }

        // First, sort by ranking to prioritize top articles
        val sortedByRanking = allValidArticles.sortedWith(
            compareByDescending<Article> { it.ranking ?: 0f }
                .thenByDescending { it.publishedAt }
        )

        // Now we need to select articles for the top list with score filtering
        val topArticles = mutableListOf<PageFormat>()
        val scoreThresholds = listOf(8.0f, 7.0f, 6.0f, 5.0f) // Continue until 6 articles are chosen

        for (threshold in scoreThresholds) {
            if (topArticles.size >= 6) break
            
            val candidates = sortedByRanking.filter { (it.ranking ?: 0f) >= threshold }
            val candidatePages = candidates.mapIndexed { index, article ->
                convertArticleToPageFormat(article, 110 + index)
            }
            
            for (page in candidatePages) {
                if (!topArticles.any { it.pageId == page.pageId } && topArticles.size < 6) {
                    topArticles.add(page)
                }
            }
        }

        // Pad with remaining articles if we don't have 6 yet
        if (topArticles.size < 6) {
            val remainingArticles = sortedByRanking.mapIndexed { index, article ->
                convertArticleToPageFormat(article, 110 + index)
            }.filter { page -> !topArticles.any { it.pageId == page.pageId } }

            for (page in remainingArticles) {
                if (topArticles.size < 6) {
                    topArticles.add(page)
                } else {
                    break
                }
            }
        }

        // For latest news, get remaining articles and sort by publication date (newest first)
        val remainingArticles = sortedByRanking.mapIndexed { index, article ->
            convertArticleToPageFormat(article, 110 + index)
        }.filter { page -> !topArticles.any { it.pageId == page.pageId } }

        val latestArticles = remainingArticles.sortedByDescending { page ->
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                sdf.parse(page.publishedAt.substring(0, 19))
            } catch (e: Exception) {
                java.util.Date(0) // fallback for invalid dates
            }
        }.take(8)

        val articlePages = sortedByRanking.mapIndexed { index, article ->
            convertArticleToPageFormat(article, 110 + index)
        }

        val mainPage = buildMainPage(topArticles.take(6), latestArticles.take(8))
        val indexPages = buildIndexPages(articlePages)

        val allPages = mutableListOf<PageFormat>()
        allPages.add(mainPage)
        allPages.addAll(indexPages)
        allPages.addAll(articlePages)

        return allPages.sortedBy { it.pageId.toInt() }
    }

    fun parse(context: Context, resourceId: Int): List<PageFormat> {
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.use { it.readText() }
        return parse(jsonString)
    }

    private fun buildIndexPages(articlePages: List<PageFormat>): List<PageFormat> {
        val indexPages = mutableListOf<PageFormat>()
        val articlesSortedByPageNumber = articlePages.sortedBy { it.pageId.toInt() }

        val articlesChunks = articlesSortedByPageNumber.chunked(10) // 10 articles per index page

        articlesChunks.take(9).forEachIndexed { index, chunk ->
            val pageNumber = 101 + index
            val sections = mutableListOf<Section>()
            var currentRow = 0

            // Add header
            sections.add(
                Section(
                    id = "index-header",
                    type = "header",
                    position = Position(row = currentRow, column = 0),
                    styles = SectionStyles(
                        foreground = color(ThemeColor.INDEX_HEADER_FG),
                        background = color(ThemeColor.INDEX_HEADER_BG),
                        maxWidth = 40
                    ),
                    content = SectionContent.Text("INDEX ${pageNumber}")
                )
            )
            currentRow += 2 // Add a blank line after header

            val twoGroups = chunk.chunked(5)

            twoGroups.forEachIndexed { groupIndex, group ->
                group.forEach { articlePage ->
                    val title = getPageTitleForDisplay(articlePage)
                    val limitedTitle = if (title.length > 32) title.substring(0, 32) else title
                    val pageNumberStr = articlePage.pageId
                    val availableSpaceForDots = 39 - limitedTitle.length - pageNumberStr.length
                    val dotsNeeded = if (availableSpaceForDots > 0) availableSpaceForDots else 0

                    val displayText = limitedTitle + ".".repeat(dotsNeeded) + "{{" + pageNumberStr + "}}"

                    sections.add(
                        Section(
                            id = "page-${articlePage.pageId}",
                            type = "text-block",
                            position = Position(row = currentRow, column = 0),
                            styles = SectionStyles(
                                foreground = color(ThemeColor.INDEX_ITEM_FG),
                                background = color(ThemeColor.INDEX_ITEM_BG),
                                maxWidth = 40
                            ),
                            content = SectionContent.Text(displayText)
                        )
                    )
                    currentRow += 1
                }
                if (groupIndex == 0 && twoGroups.size > 1) {
                    currentRow += 1 // Add a blank line between the two groups
                }
            }

            // Fill remaining lines to make 25
            while (currentRow < 25) {
                sections.add(
                    Section(
                        id = "blank-line-${currentRow}",
                        type = "text-block",
                        position = Position(row = currentRow, column = 0),
                        styles = SectionStyles(
                            foreground = color(ThemeColor.INDEX_ITEM_FG),
                            background = color(ThemeColor.INDEX_ITEM_BG),
                            maxWidth = 40
                        ),
                        content = SectionContent.Text(" ".repeat(40))
                    )
                )
                currentRow += 1
            }

            val indexPage = PageFormat(
                pageId = pageNumber.toString(),
                title = "Index ${pageNumber}",
                version = "1.0",
                publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                globalStyles = GlobalStyles(
                    defaultForeground = color(ThemeColor.INDEX_ITEM_FG),
                    defaultBackground = color(ThemeColor.INDEX_ITEM_BG),
                    defaultFont = "monospace",
                    maxPageWidth = 40,
                    maxPageHeight = 25
                ),
                sections = sections,
                links = emptyList(),
                metadata = Metadata(
                    language = "de",
                    category = "index",
                    source = null
                )
            )
            indexPages.add(indexPage)
        }

        return indexPages
    }

    private fun convertArticleToPageFormat(article: Article, assignedPageNumber: Int): PageFormat {
        val sections = mutableListOf<Section>()
        var currentRow = 0

        // Header background
        sections.add(
            Section(
                id = "header-background",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_BG),
                    background = color(ThemeColor.HEADER_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" ".repeat(40))
            )
        )

        // Topic
        val topicText = article.topic.uppercase()
        val truncatedTopic = if (topicText.length > 22) topicText.substring(0, 22) else topicText
        val paddedTopic = " ${truncatedTopic.padEnd(22)} "
        sections.add(
            Section(
                id = "topic",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.HEADER_BG),
                    maxWidth = 24
                ),
                content = SectionContent.Text(paddedTopic)
            )
        )

        // Date
        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", locale)
        val date = try {
            // Parse ISO date string manually to avoid SimpleDateFormat issues
            val isoString = article.publishedAt
            if (isoString.length >= 19) {
                val year = isoString.substring(0, 4).toInt()
                val month = isoString.substring(5, 7).toInt() - 1 // Calendar months are 0-based
                val day = isoString.substring(8, 10).toInt()
                val hour = isoString.substring(11, 13).toInt()
                val minute = isoString.substring(14, 16).toInt()
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.set(year, month, day, hour, minute, 0)
                calendar.time
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        val formattedDate = if (date != null) sdf.format(date) else "NO DATE"
        val paddedDate = " $formattedDate "
        val dateSectionWidth = paddedDate.length
        val dateSectionColumn = 40 - dateSectionWidth
        sections.add(
            Section(
                id = "date",
                type = "header",
                position = Position(row = currentRow, column = dateSectionColumn),
                styles = SectionStyles(
                    foreground = color(ThemeColor.DATE_TIME_FG),
                    background = color(ThemeColor.TOP_ARTICLES_BG),
                    maxWidth = dateSectionWidth
                ),
                content = SectionContent.Text(paddedDate)
            )
        )
        currentRow += 1

        // Title
        val titleText = if (article.mediumTitle.length <= 40) {
            article.mediumTitle
        } else {
            article.shortTitle
        }.take(40)

        sections.add(
            Section(
                id = "title",
                type = "title",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.TITLE_TEXT),
                    background = color(ThemeColor.ARTICLE_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(titleText)
            )
        )
        currentRow += 1

        // Empty line after title
        sections.add(
            Section(
                id = "blank-line-after-title",
                type = "text-block",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.ARTICLE_PAGE_BG),
                    background = color(ThemeColor.ARTICLE_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" ".repeat(40))
            )
        )
        currentRow += 1

        // Body
        var useCyan = true
        article.paragraphs.forEach { paragraph ->
            var currentColor = if (useCyan) color(ThemeColor.BODY_TEXT) else color(ThemeColor.BODY_TEXT_ALT)
            var lineCountInParagraph = 0
            var switchColorAfterThisLine = false

            paragraph.lines.forEach { line ->
                if (switchColorAfterThisLine) {
                    currentColor = if (currentColor == color(ThemeColor.BODY_TEXT)) color(ThemeColor.BODY_TEXT_ALT) else color(ThemeColor.BODY_TEXT)
                    switchColorAfterThisLine = false
                    lineCountInParagraph = 0 // Reset counter after switch
                }

                sections.add(
                    Section(
                        id = "line",
                        type = "line",
                        position = Position(row = currentRow, column = 0),
                        styles = SectionStyles(
                            foreground = currentColor,
                            background = color(ThemeColor.ARTICLE_PAGE_BG),
                            maxWidth = 40
                        ),
                        content = SectionContent.Text(line)
                    )
                )
                currentRow += 1
                lineCountInParagraph += 1

                if (lineCountInParagraph > 5 && (line.trim().endsWith(".") || line.trim().endsWith("!") || line.trim().endsWith("?"))) {
                    switchColorAfterThisLine = true
                }
            }
            useCyan = !useCyan
        }

        // Fill remaining lines
        while (currentRow < 25) {
            if (currentRow == 24) {
                // Last line shows the source with gray foreground
                val sourceText = article.source ?: ""
                val truncatedSource = if (sourceText.length > 40) sourceText.substring(0, 40) else sourceText
                val displayText = if (article.url != null) "{{URL:$truncatedSource}}" else truncatedSource.padEnd(40)
                sections.add(
                    Section(
                        id = "source-line",
                        type = "text-block",
                        position = Position(row = currentRow, column = 0),
                        styles = SectionStyles(
                            foreground = color(ThemeColor.NAVIGATION_DISABLED_FG),
                            background = color(ThemeColor.ARTICLE_PAGE_BG),
                            maxWidth = 40
                        ),
                        content = SectionContent.Text(displayText)
                    )
                )
            } else {
                sections.add(
                    Section(
                        id = "blank-line-${currentRow}",
                        type = "text-block",
                        position = Position(row = currentRow, column = 0),
                        styles = SectionStyles(
                            foreground = color(ThemeColor.NAVIGATION_BG),
                            background = color(ThemeColor.NAVIGATION_BG),
                            maxWidth = 40
                        ),
                        content = SectionContent.Text(" ".repeat(40))
                    )
                )
            }
            currentRow += 1
        }

        return PageFormat(
            pageId = assignedPageNumber.toString(),
            title = article.shortTitle,
            version = "1.0",
            publishedAt = article.publishedAt,
            globalStyles = GlobalStyles(
                defaultForeground = color(ThemeColor.ARTICLE_PAGE_FG),
                defaultBackground = color(ThemeColor.ARTICLE_PAGE_BG),
                defaultFont = "monospace",
                maxPageWidth = 40,
                maxPageHeight = 25
            ),
            sections = sections,
            links = emptyList(),
            metadata = Metadata(
                language = "de",
                category = extractCategory(article.topic),
                source = article.source,
                url = article.url,
                longTitle = article.longTitle,
                mediumTitle = article.mediumTitle,
                shortTitle = article.shortTitle
            )
        )
    }

    private fun buildMainPage(topArticles: List<PageFormat>, latestArticles: List<PageFormat>): PageFormat {
        val globalStyles = GlobalStyles(
            defaultForeground = color(ThemeColor.ARTICLE_PAGE_FG),
            defaultBackground = color(ThemeColor.MAIN_PAGE_BG), // Green background
            defaultFont = "monospace",
            maxPageWidth = 40,
            maxPageHeight = 25
        )

        val sections = mutableListOf<Section>()
        var currentRow = 0

        // Add header
        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text("  _   _ _______     ___   _ _   _ ")
            )
        )

        currentRow += 1

        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" | \\ | | ____\\ \\   / (_) (_) \\ | |")
            )
        )

        currentRow += 1

        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" |  \\| |  _|  \\ \\ / /| | | |  \\| |")
            )
        )



        currentRow += 1

        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" | |\\  | |___  \\ V / | |_| | |\\  |")
            )
        )

        currentRow += 1

        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" |_| \\_|_____|  \\_/   \\___/|_| \\_|  AI")
            )
        )
        currentRow += 1

        sections.add(
            Section(
                id = "main-header",
                type = "header",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text("")
            )
        )
        currentRow += 1

        // Add white line
        sections.add(
            Section(
                id = "white-line-1",
                type = "text-block",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.HEADER_FG),
                    background = color(ThemeColor.MAIN_PAGE_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" ".repeat(40))
            )
        )
        currentRow += 1

        // Top articles - with white background, blue title text, clickable page numbers, with dots padding
        topArticles.forEach { page ->
            // Try long, then medium, then short title until we find one that fits in 32 chars
            val title = getPageTitleForDisplay(page)
            // Limit title to max 32 chars
            val limitedTitle = if (title.length > 32) title.substring(0, 32) else title
            // Calculate space for dots padding and page number (with 1 char margin on right)
            val pageNumberStr = page.pageId
            val availableSpaceForDots = 39 - limitedTitle.length - pageNumberStr.length - 1 // 1 char margin on right
            val dotsNeeded = if (availableSpaceForDots > 0) availableSpaceForDots else 0
            
            // Create the display text with dots padding: title + dots + {{pageId}}
            val displayText = " " + limitedTitle + ".".repeat(dotsNeeded) + "{{" + pageNumberStr + "}}"
            
            sections.add(
                Section(
                    id = "page-${page.pageId}",
                    type = "text-block",
                    position = Position(row = currentRow, column = 0),
                    styles = SectionStyles(
                        foreground = color(ThemeColor.TOP_ARTICLES_FG),  // Blue foreground for titles
                        background = color(ThemeColor.TOP_ARTICLES_BG),  // White background for upper part
                        maxWidth = 40
                    ),
                    content = SectionContent.Text(displayText)
                )
            )
            currentRow += 1
        }

        // Latest news
        sections.add(
            Section(
                id = "latest-news-header",
                type = "text-block",
                position = Position(row = currentRow, column = 0),
                styles = SectionStyles(
                    foreground = color(ThemeColor.LATEST_NEWS_HEADER_FG),
                    background = color(ThemeColor.LATEST_NEWS_HEADER_BG),
                    maxWidth = 40
                ),
                content = SectionContent.Text(" LATEST NEWS ")

            )
        )
        currentRow += 1

        latestArticles.forEach { page ->
            // Try long, then medium, then short title until we find one that fits in 32 chars
            val title = getPageTitleForDisplay(page)
            // Limit title to max 32 chars
            val limitedTitle = if (title.length > 32) title.substring(0, 32) else title
            // Calculate space for dots padding and page number (with 1 char margin on right)
            val pageNumberStr = page.pageId
            val availableSpaceForDots = 40 - limitedTitle.length - pageNumberStr.length - 2 // 1 char margin on right
            val dotsNeeded = if (availableSpaceForDots > 0) availableSpaceForDots else 0
            
            // Create the display text with dots padding: title + dots + {{pageId}}
            val displayText = " " + limitedTitle + ".".repeat(dotsNeeded) + "{{" + pageNumberStr + "}}"
            
            sections.add(
                Section(
                    id = "page-${page.pageId}",
                    type = "text-block",
                    position = Position(row = currentRow, column = 0),
                    styles = SectionStyles(
                        foreground = color(ThemeColor.LATEST_NEWS_FG),  // Blue foreground for titles
                        background = color(ThemeColor.LATEST_NEWS_BG),  // White background
                        maxWidth = 40
                    ),
                    content = SectionContent.Text(displayText)
                )
            )
            currentRow += 1
        }

    //    // Index line
    //    sections.add(
    //        Section(
    //            id = "index-line",
    //            type = "text-block",
    //            position = Position(row = currentRow, column = 0),
    //            styles = SectionStyles(
    //                foreground = ColorConstants.HEADER_FG,
    //                background = ColorConstants.MAIN_PAGE_BG,
    //                maxWidth = 40
    //            ),
    //            content = SectionContent.Text("INDEX 101")
    //        )
    //    )
    //    currentRow += 1

        // Fill remaining lines to make 25
        while (currentRow < 25) {
            sections.add(
                Section(
                    id = "blank-line-${currentRow}",
                    type = "text-block",
                    position = Position(row = currentRow, column = 0),
                    styles = SectionStyles(
                        foreground = color(ThemeColor.ARTICLE_PAGE_FG),
                        background = color(ThemeColor.MAIN_PAGE_BG),
                        maxWidth = 40
                    ),
                    content = SectionContent.Text(" ".repeat(40))
                )
            )
            currentRow += 1
        }

        return PageFormat(
            pageId = "100",
            title = "Main Page",
            version = "1.0",
            publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            globalStyles = globalStyles,
            sections = sections,
            links = emptyList(),
            metadata = Metadata(
                language = "de",
                category = "main",
                source = null
            )
        )
    }

    private fun extractCategory(topic: String): String {
        return when {
            topic.contains("INLAND", ignoreCase = true) -> "inland"
            topic.contains("AUSLAND", ignoreCase = true) -> "ausland"
            topic.contains("SPORT", ignoreCase = true) -> "sport"
            topic.contains("WIRTSCHAFT", ignoreCase = true) ||
            topic.contains("ECONOMY", ignoreCase = true) -> "wirtschaft"
            else -> "general"
        }
    }

    private fun getCategoryHeaderColor(category: String): String {
        return color(ThemeColor.CATEGORY_HEADER)
    }

    
    private fun getPageTitleForDisplay(page: PageFormat): String {
        // Try long, then medium, then short title based on metadata, returning the shortest one that fits in 32 chars
        val longTitle = page.metadata.longTitle
        val mediumTitle = page.metadata.mediumTitle
        val shortTitle = page.metadata.shortTitle
        
        // Check which title fits (longest first, but return shortest that fits)
        if (longTitle != null && longTitle.length <= 32) {
            return longTitle
        }
        if (mediumTitle != null && mediumTitle.length <= 32) {
            return mediumTitle
        }
        if (shortTitle != null && shortTitle.length <= 32) {
            return shortTitle
        }
        
        // If none fit exactly, return the shortest available title (will be truncated later)
        return shortTitle ?: mediumTitle ?: longTitle ?: page.title
    }
}