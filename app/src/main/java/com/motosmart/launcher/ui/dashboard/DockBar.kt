package com.motosmart.launcher.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.ui.components.AppIcon
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun DockBar(
    dockApps: List<AppEntry>,
    ridingMode: Boolean,
    onLaunch: (AppEntry) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    val visible = dockApps.take(if (ridingMode) 3 else 6)
    MotoPanel(
        modifier = modifier,
        padding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        elevated = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockSlot(
                label = "APPS",
                modifier = Modifier.weight(1f),
                onClick = onOpenDrawer
            ) {
                Box(
                    modifier = Modifier
                        .size(if (ridingMode) 38.dp else 42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(palette.accent.copy(alpha = 0.12f))
                        .border(1.dp, palette.accent.copy(alpha = 0.18f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = "All apps",
                        tint = palette.accent,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
            visible.forEach { entry ->
                DockSlot(
                    label = entry.label,
                    modifier = Modifier.weight(1f),
                    onClick = { onLaunch(entry) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (ridingMode) 38.dp else 42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(palette.panelHigh)
                            .border(
                                1.dp,
                                palette.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            entry = entry,
                            size = if (ridingMode) 26.dp else 29.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockSlot(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val palette = LocalMotoPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 0.2.sp
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp)
        )
    }
}
