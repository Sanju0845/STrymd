package com.example

import com.example.ui.components.formatDuration
import com.example.ui.nowplaying.parseLrcLyrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFormatDuration() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("1:05", formatDuration(65000L))
        assertEquals("3:34", formatDuration(214000L))
    }

    @Test
    fun testParseLrcLyrics() {
        val lrc = """
            [00:12.50]First line
            [01:05.00]Second line
        """.trimIndent()
        val lines = parseLrcLyrics(lrc)
        assertEquals(2, lines.size)
        assertEquals(12500L, lines[0].timestampMs)
        assertEquals("First line", lines[0].text)
        assertEquals(65000L, lines[1].timestampMs)
        assertEquals("Second line", lines[1].text)
    }
}
