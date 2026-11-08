package com.motosmart.launcher.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.motosmart.launcher.data.preferences.SavedDestination
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun NavigationQuickCard(
    savedDestinations: List<SavedDestination>,
    locationLabel: String?,
    onOpenNavigation: () -> Unit,
    onNavigate: (SavedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = modifier) {
        SectionLabel(text = "NAVIGATION", trailing = "MAPS")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(23.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Where to?",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = locationLabel ?: "Search a destination",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onOpenNavigation) {
                Text("OPEN", color = palette.accent)
            }
        }
        if (savedDestinations.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedDestinations.take(4).forEach { destination ->
                    AssistChip(
                        onClick = { onNavigate(destination) },
                        label = {
                            Text(
                                text = destination.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = palette.panelHigh,
                            labelColor = palette.textSecondary,
                            leadingIconContentColor = palette.accent
                        )
                    )
                }
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Open Navigation to search and save one-touch destinations.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
        }
    }
}
