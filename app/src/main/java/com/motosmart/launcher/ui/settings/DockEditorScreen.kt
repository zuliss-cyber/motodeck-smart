package com.motosmart.launcher.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.data.preferences.AppRef
import com.motosmart.launcher.ui.components.AppIcon
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.MotoTextField
import com.motosmart.launcher.ui.components.ScreenHeader
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun DockEditorScreen(
    apps: List<AppEntry>,
    dockApps: List<AppEntry>,
    onBack: () -> Unit,
    onAdd: (AppEntry) -> Unit,
    onRemove: (AppEntry) -> Unit,
    onSetDock: (List<AppRef>) -> Unit,
    onReset: () -> Unit
) {
    val palette = LocalMotoPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    val available = apps.filter { entry ->
        dockApps.none { it.key == entry.key } &&
            (query.isBlank() || entry.label.contains(query.trim(), ignoreCase = true))
    }
    val dockFull = dockApps.size >= 6

    fun move(from: Int, to: Int) {
        if (from == to || from !in dockApps.indices || to !in dockApps.indices) return
        val refs = dockApps.map { it.ref }.toMutableList()
        val entry = refs.removeAt(from)
        refs.add(to, entry)
        onSetDock(refs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        ScreenHeader(
            title = "Dock",
            subtitle = "One-touch shortcuts",
            onBack = onBack,
            trailing = {
                TextButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("RESET", color = palette.accent)
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "dock") {
                MotoPanel(modifier = Modifier.fillMaxWidth(), glow = true, accentEdge = true) {
                    SectionLabel(
                        text = "Current dock",
                        trailing = "${dockApps.size}/6"
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "The apps button always opens the full drawer. Reorder or remove dock apps below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    if (dockApps.isEmpty()) {
                        Text(
                            text = "Using automatic system defaults while no custom dock is saved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    } else {
                        dockApps.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(color = palette.outline.copy(alpha = 0.4f))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(palette.accent.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.accent
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                AppIcon(entry = entry, size = 34.dp)
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    text = entry.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { move(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowUpward,
                                        contentDescription = "Move up",
                                        tint = if (index > 0) palette.textSecondary else palette.textMuted,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { move(index, index + 1) },
                                    enabled = index < dockApps.lastIndex,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowDownward,
                                        contentDescription = "Move down",
                                        tint = if (index < dockApps.lastIndex) {
                                            palette.textSecondary
                                        } else {
                                            palette.textMuted
                                        },
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onRemove(entry) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove from dock",
                                        tint = palette.warning,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "available-header") {
                MotoPanel(modifier = Modifier.fillMaxWidth()) {
                    SectionLabel(
                        text = "Add apps",
                        trailing = if (dockFull) "Dock full" else "Up to 6"
                    )
                    Spacer(Modifier.height(8.dp))
                    MotoTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search installed apps",
                        leadingIcon = Icons.Outlined.Search,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    )
                    if (dockFull) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "The dock is full. Remove an app before adding another.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.warning
                        )
                    }
                }
            }

            if (available.isEmpty()) {
                item(key = "available-empty") {
                    Text(
                        text = if (query.isBlank()) {
                            "Every installed app is already in the dock."
                        } else {
                            "No matching apps."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                    )
                }
            } else {
                items(items = available, key = { "available-${it.key}" }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.panel)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(entry = entry, size = 34.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = entry.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { onAdd(entry) },
                            enabled = !dockFull
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add to dock",
                                tint = if (dockFull) palette.textMuted else palette.accent,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
