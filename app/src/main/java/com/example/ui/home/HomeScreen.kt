package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Album
import com.example.domain.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.GlassSurface
import com.example.ui.components.SongRowItem
import com.example.ui.theme.AuraAccentGold
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.AuraAccentViolet
import com.example.ui.theme.GlassCardShape
import com.example.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    recentlyPlayed: List<Song>,
    recentlyAdded: List<Song>,
    mostPlayed: List<Song>,
    favorites: List<Song>,
    albums: List<Album>,
    onPlaySong: (Song, List<Song>) -> Unit,
    onNavigateToLibraryTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val dateFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    val spotlightSong = remember(allSongs) {
        allSongs.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
    ) {
        // Date and Greeting Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = dateFormatted.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Spotlight Hero Card
        if (spotlightSong != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SpotlightHeroCard(
                    song = spotlightSong,
                    onPlay = { onPlaySong(spotlightSong, allSongs) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Recently Played Section
        if (recentlyPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader(
                    title = "Recently Played",
                    actionTitle = "See All",
                    onActionClick = { onNavigateToLibraryTab("songs") },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentlyPlayed.take(10)) { song ->
                        SongMiniCard(
                            song = song,
                            onClick = { onPlaySong(song, recentlyPlayed) }
                        )
                    }
                }
            }
        }

        // Made for You (Smart Mixes)
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Made for You",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmartMixCard(
                    title = "Favorites",
                    subtitle = "${favorites.size} songs",
                    icon = Icons.Rounded.Favorite,
                    brush = Brush.linearGradient(listOf(AuraAccentRed, AuraAccentPink)),
                    onClick = {
                        if (favorites.isNotEmpty()) onPlaySong(favorites.first(), favorites)
                    },
                    modifier = Modifier.weight(1f)
                )
                SmartMixCard(
                    title = "Heavy Rotation",
                    subtitle = "${mostPlayed.size} tracks",
                    icon = Icons.Rounded.TrendingUp,
                    brush = Brush.linearGradient(listOf(AuraAccentViolet, Color(0xFF5E5CE6))),
                    onClick = {
                        if (mostPlayed.isNotEmpty()) onPlaySong(mostPlayed.first(), mostPlayed)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recently Added Songs list
        if (recentlyAdded.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader(
                    title = "Recently Added",
                    actionTitle = "Library",
                    onActionClick = { onNavigateToLibraryTab("songs") },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(recentlyAdded.take(5)) { song ->
                SongRowItem(
                    song = song,
                    onClick = { onPlaySong(song, recentlyAdded) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionTitle: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionTitle != null && onActionClick != null) {
            Text(
                text = actionTitle,
                style = MaterialTheme.typography.labelLarge,
                color = AuraAccentRed,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable { onActionClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SpotlightHeroCard(
    song: Song,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onPlay() }
            .testTag("spotlight_card"),
        shape = GlassCardShape,
        elevation = 12.dp
    ) {
        // Gradient backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AuraAccentViolet.copy(alpha = 0.55f),
                            AuraAccentRed.copy(alpha = 0.40f),
                            Color(0x22131622)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            AlbumArtImage(
                artUri = song.albumArtUri,
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SPOTLIGHT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = AuraAccentGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Play Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AuraAccentRed)
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play Spotlight",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SongMiniCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(136.dp)
            .clickable { onClick() }
            .testTag("song_card_${song.id}")
    ) {
        AlbumArtImage(
            artUri = song.albumArtUri,
            modifier = Modifier
                .size(136.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmartMixCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    brush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
