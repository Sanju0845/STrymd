package com.example.ui.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Navigation item specification for Apple Music footer.
 */
data class GlassNavItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String = "nav_item_${label.lowercase()}"
)

/**
 * Apple Music iOS footer layout:
 * - Left capsule pill containing [Home, New, Radio, Library]
 * - Right standalone circular button with vibrant accented Search icon
 * - Compact, stable, non-jumping proportions with frosted glass blur
 */
@Composable
fun GlassNavBar(
    selectedIndex: Int,
    isSearchSelected: Boolean,
    hazeState: HazeState,
    onItemSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    visible: Boolean = true,
) {
    val items = remember {
        listOf(
            GlassNavItem(label = "Home", icon = Icons.Rounded.Home),
            GlassNavItem(label = "New", icon = Icons.Rounded.GridView),
            GlassNavItem(label = "Online", icon = Icons.Rounded.Cloud),
            GlassNavItem(label = "Library", icon = Icons.Rounded.LibraryMusic)
        )
    }

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "navBarSlide"
    )

    val pillShape = RoundedCornerShape(26.dp)
    val circleShape = CircleShape
    val contentColor = if (isDarkTheme) Color.White else Color(0xFF111827)
    val glassBg = if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.86f)
    val borderBrush = remember(isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                if (isDarkTheme) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.7f),
                if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.2f)
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 6.dp)
            .graphicsLayer { translationY = offsetY.toPx() }
            .testTag("floating_glass_nav_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Navigation Capsule (Home, New, Radio, Library)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
                .clip(pillShape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 24.dp,
                        tint = if (isDarkTheme) HazeTint(Color(0xFF0A1020).copy(alpha = 0.35f))
                        else HazeTint(Color.White.copy(alpha = 0.35f))
                    )
                )
                .background(glassBg)
                .border(width = 1.dp, brush = borderBrush, shape = pillShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = (!isSearchSelected && index == selectedIndex)
                    AppleMusicTabItem(
                        item = item,
                        selected = isSelected,
                        contentColor = contentColor,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Standalone Search Circular Button
        val searchTint = Color(0xFFFA2D48) // Apple Music vibrant magenta/red accent
        val searchBg = if (isSearchSelected) {
            searchTint.copy(alpha = 0.22f)
        } else {
            glassBg
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = circleShape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
                .clip(circleShape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 24.dp,
                        tint = if (isDarkTheme) HazeTint(Color(0xFF0A1020).copy(alpha = 0.35f))
                        else HazeTint(Color.White.copy(alpha = 0.35f))
                    )
                )
                .background(searchBg)
                .border(
                    width = 1.dp,
                    brush = if (isSearchSelected) {
                        Brush.verticalGradient(listOf(searchTint.copy(alpha = 0.6f), searchTint.copy(alpha = 0.2f)))
                    } else {
                        borderBrush
                    },
                    shape = circleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                )
                .testTag("nav_item_search"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = searchTint,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun AppleMusicTabItem(
    item: GlassNavItem,
    selected: Boolean,
    contentColor: Color,
    onClick: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.52f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navTabAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp)
            .graphicsLayer { this.alpha = alpha }
            .testTag(item.testTag)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = item.label,
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(top = 1.5.dp)
        )
    }
}
