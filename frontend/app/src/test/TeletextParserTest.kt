package com.kdr.nuven

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.json.Json

class TeletextParserTest {

    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testPageDeserialization() {
        val jsonString = """
        {
          "pageNumber": 100,
          "shortHeadline": "TEST",
          "layout": {
            "lines": 24,
            "lineLength": 40,
            "headerLines": 1,
            "titleLines": 1,
            "paragraphLines": 3
          },
          "header": {
            "text": "TEST HEADER",
            "color": "cyan"
          },
          "title": {
            "text": "TEST TITLE",
            "color": "yellow"
          },
          "entries": [
            {
              "text": "Test entry",
              "color": "white"
            }
          ]
        }
        """.trimIndent()

        val page = json.decodeFromString<Page>(jsonString)
        
        assertEquals(100, page.pageNumber)
        assertEquals("TEST", page.shortHeadline)
        assertEquals(24, page.layout.lines)
        assertEquals(40, page.layout.lineLength)
        assertEquals("TEST HEADER", page.header.text)
        assertEquals("cyan", page.header.color)
        assertEquals("TEST TITLE", page.title.text)
        assertEquals(1, page.entries.size)
        assertEquals("Test entry", page.entries[0].text)
    }

    @Test
    fun testHeadlineDeserialization() {
        val jsonString = """
        {
          "title": "TEST HEADLINE",
          "pageNumber": 100
        }
        """.trimIndent()

        val headline = json.decodeFromString<Headline>(jsonString)
        
        assertEquals("TEST HEADLINE", headline.title)
        assertEquals(100, headline.pageNumber)
    }
}
