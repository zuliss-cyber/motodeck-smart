package com.motosmart.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun AppIcon(
    entry: AppEntry,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    val image = remember(entry.key, entry.icon) {
        runCatching { entry.icon.toBitmap(128, 128).asImageBitmap() }.getOrNull()
    }
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = entry.label,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.26f)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.26f))
                .background(palette.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value * 0.38f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = palette.accent
            )
        }
    }
}
