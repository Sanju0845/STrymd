package com.example.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState

/**
 * Navigation item specification for [GlassNavBar].
 */
data class GlassNavItem(
    val label       : String,
    val icon        : ImageVector,
    val selectedIcon: ImageVector = icon,
    val testTag     : String      = "nav_item_${label.lowercase()}"
)

/**
 * Floating pill-shaped glass bottom navigation bar.
 *
 * Samples backdrop content marked with `Modifier.hazeSource(hazeState)` and renders
 * a suspended liquid glass pill with specular rim and spring-animated tab elements.
 */
@Composable
fun GlassNavBar(
    items           : List<GlassNavItem>,
    selectedIndex   : Int,
    hazeState       : HazeState,
    onItemSelected  : (Int) -> Unit,
    modifier        : Modifier = Modifier,
    isDarkTheme     : Boolean  = false,
    visible         : Boolean  = true,
) {
    val offsetY by animateDpAsState(
        targetValue  = if (visible) 0.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "navBarSlide"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp)
            .graphicsLayer { translationY = offsetY.toPx() }
            .testTag("floating_glass_nav_bar")
    ) {
        GlassSurface(
            hazeState    = hazeState,
            modifier     = Modifier
                .fillMaxWidth()
                .height(64.dp),
            cornerRadius = GlassShapes.pill,
            blurRadius   = GlassBlur.thick,
            overlayAlpha = 0.20f,
            isDarkTheme  = isDarkTheme,
        ) {
            Row(
                modifier             = Modifier
                    .fillMaxSize()
                    .selectableGroup()
                    .padding(horizontal = 8.dp),
                horizontalArrangement= Arrangement.SpaceEvenly,
                verticalAlignment    = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    GlassNavItemView(
                        item        = item,
                        selected    = index == selectedIndex,
                        isDarkTheme = isDarkTheme,
                        onClick     = { onItemSelected(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassNavItemView(
    item       : GlassNavItem,
    selected   : Boolean,
    isDarkTheme: Boolean,
    onClick    : () -> Unit,
) {
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.45f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "navScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .testTag(item.testTag)
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        AnimatedVisibility(visible = selected) {
            Text(
                text       = item.label,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color      = contentColor,
                modifier   = Modifier.padding(top = 2.dp)
            )
        }
    }
}
