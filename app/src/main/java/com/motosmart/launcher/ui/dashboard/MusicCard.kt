package com.motosmart.launcher.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.music.MusicState
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun MusicCard(
    state: MusicState,
    onEnableAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = modifier) {
        SectionLabel(
            text = "NOW PLAYING",
            trailing = if (state.isConnected) "MEDIA SESSION" else "STANDBY"
        )
        Spacer(Modifier.height(12.dp))

        if (!state.hasAccess) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Music access not enabled",
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Allow notification access to read the active media session. Notification content is never stored or uploaded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                TextButton(onClick = onEnableAccess) {
                    Text("ENABLE MUSIC ACCESS", color = palette.accent)
                }
            }
            return@MotoPanel
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArt(
                bitmap = state.artwork,
                modifier = Modifier.size(78.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title ?: "Nothing playing",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.artist ?: if (state.isConnected) {
                        "Unknown artist"
                    } else {
                        "Start music in any media app"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.album != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.album,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onOpenMusicApp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Open music app",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = state.isConnected && state.canSkipPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous track",
                    tint = if (state.isConnected) palette.textPrimary else palette.textMuted
                )
            }
            Spacer(Modifier.width(12.dp))
            FilledIconButton(
                onClick = onPlayPause,
                enabled = state.isConnected,
                modifier = Modifier.size(54.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = palette.accent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = palette.accent.copy(alpha = 0.22f),
                    disabledContentColor = palette.textMuted
                )
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = onNext,
                enabled = state.isConnected && state.canSkipNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next track",
                    tint = if (state.isConnected) palette.textPrimary else palette.textMuted
                )
            }
        }
    }
}

@Composable
private fun AlbumArt(
    bitmap: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.accent.copy(alpha = 0.32f),
                        palette.panelHigh,
                        palette.accentSecondary.copy(alpha = 0.18f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Album artwork",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
