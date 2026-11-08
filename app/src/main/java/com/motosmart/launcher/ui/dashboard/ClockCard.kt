package com.motosmart.launcher.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.UnitFormatter
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun LiveClockCard(
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val time by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(1_000L)
        }
    }
    ClockCard(
        time = time,
        date = LocalDate.now(),
        use24Hour = use24Hour,
        modifier = modifier,
        compact = compact
    )
}

/**
 * Compact race-cockpit clock. The type scales down in landscape and riding mode so the
 * dashboard keeps its speed/telemetry hierarchy instead of being dominated by the time.
 */
@Composable
fun ClockCard(
    time: LocalTime,
    date: LocalDate,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val palette = LocalMotoPalette.current
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val timeSize = when {
        compact && landscape -> 34.sp
        compact -> 40.sp
        landscape -> 40.sp
        else -> 52.sp
    }
    val secondsSize = if (landscape || compact) 12.sp else 14.sp

    MotoPanel(
        modifier = modifier,
        padding = PaddingValues(
            horizontal = 16.dp,
            vertical = if (compact) 9.dp else 12.dp
        ),
        accentEdge = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel(
                    text = "CLOCK",
                    trailing = if (use24Hour) "24H" else "12H"
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = UnitFormatter.clock(time, use24Hour),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = timeSize,
                            lineHeight = (timeSize.value * 1.02f).sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = (-1.5).sp
                        ),
                        color = palette.textPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            text = UnitFormatter.seconds(time),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = secondsSize,
                                lineHeight = (secondsSize.value * 1.1f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = palette.accent
                        )
                        if (!use24Hour) {
                            Text(
                                text = UnitFormatter.period(time),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    lineHeight = 8.sp
                                ),
                                color = palette.textMuted
                            )
                        }
                    }
                }
            }
            if (!compact) {
                Text(
                    text = "LOCAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp,
                        letterSpacing = 1.6.sp
                    ),
                    color = palette.accent,
                    textAlign = TextAlign.End
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = UnitFormatter.date(date).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 8.sp else 9.sp,
                    letterSpacing = 0.8.sp
                ),
                color = palette.textSecondary
            )
            Text(
                text = if (use24Hour) "24H" else "12H",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 7.sp,
                    letterSpacing = 1.2.sp
                ),
                color = palette.textMuted
            )
        }
    }
}
