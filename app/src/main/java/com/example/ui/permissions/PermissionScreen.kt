package com.example.ui.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassSurface
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.AuraAccentViolet
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.GlassCardShape
import com.example.ui.theme.PillShape

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionDeniedOnce by remember { mutableStateOf(false) }

    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            permissionDeniedOnce = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AuraAccentViolet.copy(alpha = 0.25f),
                        AuraDarkBackground,
                        AuraDarkBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App branding badge
            GlassSurface(
                modifier = Modifier.size(96.dp),
                shape = PillShape,
                elevation = 16.dp
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = AuraAccentRed,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome to STrymd",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pure local sound. Translucent liquid glass design.",
                style = MaterialTheme.typography.titleMedium,
                color = AuraAccentPink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = GlassCardShape,
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FeatureRow(
                        icon = Icons.Rounded.FolderSpecial,
                        title = "Universal Audio Access",
                        subtitle = "Plays files from Music, Downloads, WhatsApp, and SD cards without internet."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureRow(
                        icon = Icons.Rounded.LibraryMusic,
                        title = "Smart Local Library",
                        subtitle = "Auto-organizes Albums, Artists, Playlists, and listening statistics."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureRow(
                        icon = Icons.Rounded.Lock,
                        title = "100% Private & Offline",
                        subtitle = "No accounts, no external tracking, no cloud latency."
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Primary action button
            Button(
                onClick = {
                    if (permissionDeniedOnce) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        permissionLauncher.launch(requiredPermission)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("allow_music_access_button"),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = AuraAccentRed)
            ) {
                Text(
                    text = if (permissionDeniedOnce) "Open App Settings" else "Allow Music Access",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary option to continue right away
            OutlinedButton(
                onClick = onPermissionGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("explore_library_button"),
                shape = PillShape
            ) {
                Text(
                    text = "Explore Aura with Demo Music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AuraAccentRed.copy(alpha = 0.15f), shape = PillShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraAccentRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
