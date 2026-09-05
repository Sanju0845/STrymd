package com.example.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraAccentRed

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

@Composable
fun LyricsView(
    lyricsText: String?,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedLines = remember(lyricsText) {
        parseLrcLyrics(lyricsText)
    }

    if (parsedLines.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "No Lyrics Available",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Embed LRC tags into your local audio files to view real-time lyrics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    val activeIndex = remember(currentPositionMs, parsedLines) {
        val idx = parsedLines.indexOfLast { it.timestampMs <= currentPositionMs }
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex in parsedLines.indices) {
            listState.animateScrollToItem(
                index = (activeIndex - 2).coerceAtLeast(0)
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("lyrics_list")
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }

        itemsIndexed(parsedLines) { index, line ->
            val isActive = index == activeIndex

            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                animationSpec = spring(),
                label = "lyricColor"
            )

            val fontSize = if (isActive) 26.sp else 20.sp
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

            Text(
                text = line.text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = if (isActive) 34.sp else 28.sp,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (line.timestampMs >= 0) {
                            onSeek(line.timestampMs)
                        }
                    }
                    .padding(vertical = 12.dp)
                    .testTag("lyric_line_$index")
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

fun parseLrcLyrics(rawLyrics: String?): List<LyricLine> {
    if (rawLyrics.isNullOrBlank()) return emptyList()

    val lines = rawLyrics.lines()
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
    val parsed = mutableListOf<LyricLine>()

    for (line in lines) {
        val match = regex.find(line.trim())
        if (match != null) {
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val msPart = match.groupValues[3]
            val ms = if (msPart.length == 2) msPart.toLong() * 10 else msPart.toLong()
            val totalMs = (min * 60 + sec) * 1000 + ms
            val text = match.groupValues[4].trim()
            if (text.isNotEmpty()) {
                parsed.add(LyricLine(totalMs, text))
            }
        } else if (line.isNotBlank() && !line.startsWith("[")) {
            parsed.add(LyricLine(-1L, line.trim()))
        }
    }

    return parsed.sortedBy { it.timestampMs }
}
