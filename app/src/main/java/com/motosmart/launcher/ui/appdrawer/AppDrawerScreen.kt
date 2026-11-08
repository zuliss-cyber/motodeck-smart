@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.motosmart.launcher.ui.appdrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.data.preferences.AppRef
import com.motosmart.launcher.ui.components.AppIcon
import com.motosmart.launcher.ui.components.EmptyState
import com.motosmart.launcher.ui.components.ScreenHeader
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun AppDrawerScreen(
    apps: List<AppEntry>,
    favouriteApps: List<AppEntry>,
    favouriteRefs: List<AppRef>,
    dockApps: List<AppEntry>,
    dockRefs: List<AppRef>,
    onBack: () -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onToggleFavourite: (AppEntry) -> Unit,
    onAddToDock: (AppEntry) -> Unit,
    onRemoveFromDock: (AppEntry) -> Unit,
    onOpenAppInfo: (AppRef) -> Unit
) {
    val palette = LocalMotoPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<AppEntry?>(null) }

    val filtered = remember(apps, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            apps
        } else {
            apps.filter { it.label.contains(trimmed, ignoreCase = true) }
        }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { entry ->
            entry.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
        }.toSortedMap()
    }
    val searching = query.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        ScreenHeader(
            title = "Applications",
            subtitle = "${apps.size} installed",
            onBack = onBack,
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.Apps,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search apps") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.outline,
                focusedContainerColor = palette.panel,
                unfocusedContainerColor = palette.panel,
                cursorColor = palette.accent,
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                focusedPlaceholderColor = palette.textMuted,
                unfocusedPlaceholderColor = palette.textMuted,
                focusedLeadingIconColor = palette.accent,
                unfocusedLeadingIconColor = palette.textMuted,
                focusedTrailingIconColor = palette.textSecondary,
                unfocusedTrailingIconColor = palette.textSecondary
            )
        )
        Spacer(Modifier.height(8.dp))

        if (apps.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Apps,
                title = "No apps found",
                message = "Android did not return any launchable applications for this user profile.",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 82.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (!searching) {
                    item(key = "favourites-header", span = { GridItemSpan(maxLineSpan) }) {
                        DrawerSectionHeader(
                            title = "Favourites",
                            detail = favouriteApps.size.toString()
                        )
                    }
                    if (favouriteApps.isEmpty()) {
                        item(key = "favourites-empty", span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Long-press any app to add it to favourites or the dock.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        items(
                            items = favouriteApps,
                            key = { "fav-${it.key}" }
                        ) { entry ->
                            AppTile(
                                entry = entry,
                                onClick = { onLaunch(entry) },
                                onLongClick = { selected = entry }
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item(key = "no-results", span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            icon = Icons.Outlined.Search,
                            title = "No matching apps",
                            message = "Try a different name.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                        )
                    }
                } else {
                    grouped.forEach { (letter, group) ->
                        if (!searching) {
                            item(key = "header-$letter", span = { GridItemSpan(maxLineSpan) }) {
                                DrawerSectionHeader(
                                    title = letter,
                                    detail = group.size.toString()
                                )
                            }
                        }
                        items(
                            items = group,
                            key = { "all-${it.key}" }
                        ) { entry ->
                            AppTile(
                                entry = entry,
                                onClick = { onLaunch(entry) },
                                onLongClick = { selected = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    selected?.let { entry ->
        val isFavourite = favouriteRefs.any { it == entry.ref }
        val inDock = dockRefs.any { it == entry.ref } || dockApps.any { it.key == entry.key }
        val dockFull = dockApps.size >= 6
        AlertDialog(
            onDismissRequest = { selected = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(entry = entry, size = 34.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column {
                    DialogAction(
                        icon = if (isFavourite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        label = if (isFavourite) "Remove from favourites" else "Add to favourites",
                        onClick = {
                            onToggleFavourite(entry)
                            selected = null
                        }
                    )
                    DialogAction(
                        icon = if (inDock) Icons.Outlined.DeleteOutline else Icons.Outlined.Add,
                        label = when {
                            inDock -> "Remove from dock"
                            dockFull -> "Dock is full"
                            else -> "Add to dock"
                        },
                        enabled = inDock || !dockFull,
                        onClick = {
                            if (inDock) onRemoveFromDock(entry) else onAddToDock(entry)
                            selected = null
                        }
                    )
                    DialogAction(
                        icon = Icons.Outlined.Info,
                        label = "App info",
                        onClick = {
                            onOpenAppInfo(entry.ref)
                            selected = null
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("CLOSE", color = palette.accent)
                }
            }
        )
    }
}

@Composable
private fun DrawerSectionHeader(title: String, detail: String) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(palette.accent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
    }
}

@Composable
private fun AppTile(
    entry: AppEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val palette = LocalMotoPalette.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(palette.panel)
                .border(1.dp, palette.outline.copy(alpha = 0.35f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(entry = entry, size = 42.dp)
        }
        Text(
            text = entry.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.1.sp
            ),
            color = palette.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp, start = 2.dp, end = 2.dp)
        )
    }
}

@Composable
private fun DialogAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val palette = LocalMotoPalette.current
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) palette.accent else palette.textMuted,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            color = if (enabled) palette.textPrimary else palette.textMuted,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}
