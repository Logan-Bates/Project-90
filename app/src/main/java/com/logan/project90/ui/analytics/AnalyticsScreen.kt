package com.logan.project90.ui.analytics

import androidx.compose.runtime.Composable
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText

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
        ScreenSection(title = "Current Experiment") {
            LabeledValue("Current Experiment", uiState.overview.experiment?.name ?: "Not set up yet")
            LabeledValue("Experiment Momentum", uiState.weightedMomentumDisplay)
            LabeledValue("Identity Overview", uiState.overview.identityCount.toString())
            if (uiState.overview.totalFloorMinutes > 0) {
                LabeledValue("Daily Floor Load", "${uiState.overview.totalFloorMinutes} min")
            }
        }
        ScreenSection(title = "Identity Overview") {
            if (uiState.overview.identitySummaries.isEmpty()) {
                SupportText(text = "Add an identity in Experiment to start tracking progress.")
            } else {
                uiState.overview.identitySummaries.forEach { summary ->
                    AnalyticsIdentityCard(summary = summary, onOpenIdentity = onOpenIdentity)
                }
            }
        }
    }
}
