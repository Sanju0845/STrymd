package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.FloatingNavShape
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.PillShape
import kotlin.math.roundToInt

enum class AuraNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Rounded.Home),
    LIBRARY("library", "Library", Icons.Rounded.LibraryMusic),
    SEARCH("search", "Search", Icons.Rounded.Search),
    SETTINGS("settings", "Settings", Icons.Rounded.Settings)
}

@Composable
fun FloatingNavBar(
    selectedIndex: Int,
    onNavigateIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val destinations = AuraNavDestination.values()

    // Smooth spring-based slider position that effortlessly glides across tabs
    val animatedPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        ),
        label = "nav_slider_position"
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .testTag("floating_nav_bar"),
        shape = FloatingNavShape,
        elevation = 20.dp,
        borderWidth = 1.4.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val tabWidth = maxWidth / destinations.size
            val density = LocalDensity.current
            val tabWidthPx = with(density) { tabWidth.toPx() }

            // Smooth sliding Liquid Glass capsule indicator
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (tabWidthPx * animatedPosition).roundToInt(),
                            y = 0
                        )
                    }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(PillShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AuraAccentRed.copy(alpha = if (isDark) 0.28f else 0.22f),
                                AuraAccentPink.copy(alpha = if (isDark) 0.14f else 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(tabWidthPx / 2f, tabWidthPx / 3f),
                            radius = tabWidthPx * 0.9f
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.40f else 0.70f),
                                AuraAccentRed.copy(alpha = 0.50f),
                                Color.Transparent
                            )
                        ),
                        shape = PillShape
                    )
                    .drawWithContent {
                        drawContent()
                        // Specular top highlight arc on active capsule
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.25f else 0.55f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width * 0.4f,
                            center = Offset(size.width / 2f, 0f)
                        )
                    }
            )

            // Tab items row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = selectedIndex == index

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) AuraAccentRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "iconTint"
                    )

                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.06f else 0.96f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                        label = "itemScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigateIndex(index)
                            }
                            .testTag("nav_item_${destination.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.scale(itemScale)
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = iconTint
                            )
                        }
                    }
                }
            }
        }
    }
}
