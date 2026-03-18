package com.logan.project90.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.logan.project90.core.model.displayName
import com.logan.project90.domain.model.AnalyticsActivityState
import com.logan.project90.domain.model.AnalyticsIdentitySummary
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.SupportText
import java.util.Locale

@Composable
fun AnalyticsIdentityCard(
    summary: AnalyticsIdentitySummary,
    onOpenIdentity: (Long) -> Unit
) {
    AppCard {
        Text(
            text = summary.identity.name,
            style = MaterialTheme.typography.titleSmall
        )
        LabeledValue("Category", summary.identity.category.displayName())
        LabeledValue("Consistency Score", formatScore(summary.strength14))
        LabeledValue("Momentum Score", formatScore(summary.momentum))

        TrendSection(
            title = "Momentum Trend",
            values = summary.momentumTrend
        )
        TrendSection(
            title = "Consistency Trend",
            values = summary.strengthTrend
        )

        Text(
            text = "Recent Pattern",
            style = MaterialTheme.typography.labelLarge
        )
        LabeledValue("Days Logged", summary.consistencySummary.loggedDays.toString())
        LabeledValue("Floor Days", summary.consistencySummary.floorDays.toString())
        LabeledValue("Push Days", summary.consistencySummary.pushDays.toString())
        LabeledValue("Recovery Balance", formatScore(summary.consistencySummary.recoveryBalance14))

        Text(
            text = "Last 7 Days",
            style = MaterialTheme.typography.labelLarge
        )
        if (summary.recentActivity.isEmpty()) {
            SupportText(text = "More check-ins will reveal the pattern.")
        } else {
            RecentActivityStrip(states = summary.recentActivity)
        }

        TextButton(onClick = { onOpenIdentity(summary.identity.id) }) {
            Text("See Details")
        }
    }
}

@Composable
private fun TrendSection(
    title: String,
    values: List<Double>
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge
    )
    if (values.size < 2) {
        SupportText(text = "More check-ins will reveal the pattern.")
    } else {
        TrendSparkline(values = values)
    }
}

@Composable
private fun RecentActivityStrip(states: List<AnalyticsActivityState>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        states.forEach { state ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(activityColor(state))
            )
        }
    }
}

@Composable
private fun activityColor(state: AnalyticsActivityState) =
    when (state) {
        AnalyticsActivityState.NO_LOG -> MaterialTheme.colorScheme.surfaceVariant
        AnalyticsActivityState.MISSED -> MaterialTheme.colorScheme.errorContainer
        AnalyticsActivityState.FLOOR -> MaterialTheme.colorScheme.secondaryContainer
        AnalyticsActivityState.PUSH -> MaterialTheme.colorScheme.primaryContainer
    }

private fun formatScore(value: Double): String =
    String.format(Locale.US, "%.1f", value)
