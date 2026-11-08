package com.motosmart.launcher.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.motosmart.launcher.data.location.GpsState
import com.motosmart.launcher.data.location.LocationLabelResolver
import com.motosmart.launcher.data.navigation.NavigationLauncher
import com.motosmart.launcher.data.preferences.SavedDestination
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.MotoTextField
import com.motosmart.launcher.ui.components.ScreenHeader
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun NavigationScreen(
    gps: GpsState,
    locationLabel: String?,
    savedDestinations: List<SavedDestination>,
    onBack: () -> Unit,
    onSaveDestination: (SavedDestination) -> Unit,
    onRemoveDestination: (SavedDestination) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalMotoPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun search() {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            toast("Enter a destination to search")
            return
        }
        if (!NavigationLauncher.openSearch(context, trimmed)) {
            toast("No maps or browser app available")
        }
    }

    fun saveDestination() {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            toast("Enter a name for this destination")
            return
        }
        val hasLatitude = latitude.isNotBlank()
        val hasLongitude = longitude.isNotBlank()
        if (hasLatitude != hasLongitude) {
            toast("Enter both latitude and longitude, or leave both empty")
            return
        }
        var latValue: Double? = null
        var lonValue: Double? = null
        if (hasLatitude) {
            val parsedLat = latitude.trim().toDoubleOrNull()
            val parsedLon = longitude.trim().toDoubleOrNull()
            if (parsedLat == null || parsedLon == null ||
                parsedLat !in -90.0..90.0 || parsedLon !in -180.0..180.0
            ) {
                toast("Coordinates are not valid")
                return
            }
            latValue = parsedLat
            lonValue = parsedLon
        }
        onSaveDestination(SavedDestination(trimmed, latValue, lonValue))
        label = ""
        latitude = ""
        longitude = ""
        toast("Destination saved")
    }

    fun saveCurrentLocation() {
        val point = gps.point
        if (point == null) {
            toast("Waiting for a GPS position")
            return
        }
        val name = locationLabel ?: LocationLabelResolver.formatCoordinates(point)
        onSaveDestination(SavedDestination(name, point.latitude, point.longitude))
        toast("Current location saved")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        ScreenHeader(
            title = "Navigation",
            subtitle = "Maps via Android intents",
            onBack = onBack,
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MotoPanel(modifier = Modifier.fillMaxWidth(), glow = true, accentEdge = true) {
                SectionLabel(text = "Destination search", trailing = "Maps")
                Spacer(Modifier.height(10.dp))
                MotoTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search a place or address",
                    leadingIcon = Icons.Outlined.Search,
                    modifier = Modifier.fillMaxWidth(),
                    imeAction = ImeAction.Search,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search() })
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { search() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("SEARCH IN GOOGLE MAPS")
                }
                TextButton(
                    onClick = {
                        if (!NavigationLauncher.openGoogleMaps(context)) {
                            toast("No maps or browser app available")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("LAUNCH GOOGLE MAPS", color = palette.accent)
                }
            }

            MotoPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(
                    text = "Current position",
                    trailing = if (gps.hasFix) "GPS LOCK" else "NO FIX"
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = if (gps.hasFix) palette.accent else palette.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = locationLabel ?: "No location yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = gps.point?.let { LocationLabelResolver.formatCoordinates(it) }
                                ?: "Waiting for a GPS position",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = palette.textSecondary
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val point = gps.point
                            val launched = if (point != null) {
                                NavigationLauncher.showOnMap(context, point, locationLabel)
                            } else {
                                NavigationLauncher.openGoogleMaps(context)
                            }
                            if (!launched) toast("No maps or browser app available")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OPEN MAP")
                    }
                    OutlinedButton(
                        onClick = { saveCurrentLocation() },
                        enabled = gps.point != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SAVE HERE")
                    }
                }
            }

            MotoPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(
                    text = "Saved destinations",
                    trailing = "${savedDestinations.size}/12"
                )
                Spacer(Modifier.height(6.dp))
                if (savedDestinations.isEmpty()) {
                    Text(
                        text = "No saved destinations yet. Search for a place, or save your current position for one-touch access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                } else {
                    savedDestinations.forEachIndexed { index, destination ->
                        if (index > 0) {
                            HorizontalDivider(color = palette.outline.copy(alpha = 0.4f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!NavigationLauncher.navigate(context, destination)) {
                                        toast("No maps or browser app available")
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Navigation,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val coords = if (destination.latitude != null &&
                                    destination.longitude != null
                                ) {
                                    "${destination.latitude}, ${destination.longitude}"
                                } else {
                                    "Search by name"
                                }
                                Text(
                                    text = coords,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = palette.textMuted
                                )
                            }
                            IconButton(onClick = { onRemoveDestination(destination) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Remove destination",
                                    tint = palette.textMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }

            MotoPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(text = "Save a destination", trailing = "Local")
                Spacer(Modifier.height(10.dp))
                MotoTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = "Name, for example \"Home\"",
                    leadingIcon = Icons.Outlined.LocationOn,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MotoTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        placeholder = "Latitude",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        imeAction = ImeAction.Next
                    )
                    MotoTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        placeholder = "Longitude",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Coordinates are optional. Leave both blank to save a search-only destination.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { saveDestination() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("SAVE DESTINATION")
                }
            }

            Text(
                text = "Maps opens through an Android intent. No Google Maps API key is embedded in this app.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}
