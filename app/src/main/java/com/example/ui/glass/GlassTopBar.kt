package com.example.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState

/**
 * Frosted liquid glass top app bar that blurs the list content beneath it as you scroll.
 */
@Composable
fun GlassTopBar(
    title       : String,
    hazeState   : HazeState,
    modifier    : Modifier  = Modifier,
    isDarkTheme : Boolean   = false,
    actions     : @Composable RowScope.() -> Unit = {}
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    GlassSurface(
        hazeState    = hazeState,
        modifier     = modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        cornerRadius = 0.dp,
        blurRadius   = GlassBlur.regular,
        overlayAlpha = 0.14f,
        showBorder   = false,
        isDarkTheme  = isDarkTheme,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(top = topInset)
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text       = title,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isDarkTheme) Color.White else Color.Black,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}
