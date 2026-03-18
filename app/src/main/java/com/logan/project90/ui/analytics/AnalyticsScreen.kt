package com.logan.project90.ui.analytics

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onOpenIdentity: (Long) -> Unit
) {
    AppScreen(scrollable = true) {
        ScreenIntro(
            title = "Analytics",
            subtitle = "Scan experiment momentum and compare each identity at a glance."
        )
        ScreenSection(title = "Experiment") {
            LabeledValue("Experiment", uiState.overview.experiment?.name ?: "Not created")
            LabeledValue("Momentum", formatScore(uiState.overview.weightedMomentum))
            LabeledValue("Identities", uiState.overview.identityCount.toString())
            if (uiState.overview.totalFloorMinutes > 0) {
                LabeledValue("Total Floor", "${uiState.overview.totalFloorMinutes} min")
            }
        }
        ScreenSection(title = "Identities") {
            if (uiState.overview.identitySummaries.isEmpty()) {
                SupportText(text = "No identities yet. Add one in Experiment to start building metrics.")
            } else {
                uiState.overview.identitySummaries.forEach { summary ->
                    AppCard {
                        Text(
                            text = summary.identity.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        LabeledValue("Category", summary.identity.category.name)
                        LabeledValue("Strength14", formatScore(summary.strength14))
                        LabeledValue("Momentum", formatScore(summary.momentum))
                        TextButton(onClick = { onOpenIdentity(summary.identity.id) }) {
                            Text("View Identity Details")
                        }
                    }
                }
            }
        }
    }
}

private fun formatScore(value: Double): String =
    String.format(Locale.US, "%.1f", value)
