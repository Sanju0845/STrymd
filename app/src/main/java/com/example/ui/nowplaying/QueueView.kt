package com.example.ui.nowplaying

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.theme.AuraAccentRed
import kotlin.math.roundToInt

@Composable
fun QueueView(
    queue: List<Song>,
    currentIndex: Int,
    onSongSelected: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onReorderQueue: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 64.dp.toPx() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("queue_list")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Playing Next",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
            val isCurrent = index == currentIndex
            val isDragging = draggedIndex == index

            val scale by animateFloatAsState(
                targetValue = if (isDragging) 1.06f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "queueItemScale"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (isDragging) {
                            translationY = dragOffset
                            shadowElevation = 24f
                        }
                    }
                    .scale(scale)
                    .background(
                        color = if (isDragging) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggedIndex?.let { from ->
                                    val targetIndex = (from + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, queue.size - 1)
                                    if (from != targetIndex) {
                                        onReorderQueue(from, targetIndex)
                                    }
                                }
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                            }
                        )
                    }
                    .clickable { if (draggedIndex == null) onSongSelected(song) }
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .testTag("queue_item_$index"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    artUri = song.albumArtUri,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Playing",
                                tint = AuraAccentRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCurrent) AuraAccentRed else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isCurrent) {
                    IconButton(
                        onClick = { onRemoveFromQueue(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove from Queue",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
