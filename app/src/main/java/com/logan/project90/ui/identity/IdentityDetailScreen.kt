package com.logan.project90.ui.identity

import androidx.compose.runtime.Composable
import com.logan.project90.core.model.displayName
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.FeedbackMessage
import com.logan.project90.domain.model.FeedbackType
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText
import java.util.Locale

@Composable
fun IdentityDetailScreen(
    uiState: IdentityDetailUiState
) {
    AppScreen(scrollable = true) {
        ScreenIntro(
            title = uiState.detail?.identity?.name ?: "Identity Detail",
            subtitle = "Review the identity, current analytics, and recent logged activity."
        )
        uiState.error?.let {
            InlineMessage(text = it, tone = MessageTone.ERROR)
        }
        uiState.detail?.let { detail ->
            ScreenSection(title = "Identity") {
                LabeledValue("Category", detail.identity.category.displayName())
                SupportText(text = detail.identity.statement)
            }
            ScreenSection(title = "Targets") {
                LabeledValue("Floor", "${detail.identity.floorMinutes} min")
                LabeledValue("Push", "${detail.identity.pushMinutes} min")
                LabeledValue("Priority", detail.identity.importanceWeight.toString())
            }
            ScreenSection(title = "Analytics") {
                LabeledValue("Consistency Score", formatScore(detail.analytics.strength14))
                LabeledValue("Weekly Consistency", formatScore(detail.analytics.strength7))
                LabeledValue("Push Rate", formatScore(detail.analytics.pushFreq14))
                LabeledValue("Recovery Balance", formatScore(detail.analytics.recoveryBalance14))
                LabeledValue("Momentum Score", formatScore(detail.analytics.momentum))
            }
            if (detail.feedback.isNotEmpty()) {
                ScreenSection(title = "What the pattern suggests") {
                    detail.feedback.forEach { feedback ->
                        androidx.compose.material3.Text(
                            text = feedback.title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                        )
                        InlineMessage(
                            text = feedback.message,
                            tone = feedback.messageTone()
                        )
                    }
                }
            }
            ScreenSection(title = "Last 7 Days") {
                if (detail.recentLogs.isEmpty()) {
                    SupportText(text = "No activity recorded yet.")
                } else {
                    detail.recentLogs.forEach { log ->
                        RecentLogRow(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentLogRow(log: DailyLog) {
    com.logan.project90.ui.components.AppCard {
        androidx.compose.material3.Text(
            text = log.logDate.toString(),
            style = androidx.compose.material3.MaterialTheme.typography.titleSmall
        )
        LabeledValue("Status", log.status.displayName())
        LabeledValue("Effort", "${log.effortMinutes} min")
        LabeledValue("Energy / Mood", "${log.energy} / ${log.mood}")
        LabeledValue("Resistance", log.resistance.displayName())
        if (log.reflection.isNotBlank()) {
            SupportText(text = log.reflection)
        }
    }
}

private fun formatScore(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun FeedbackMessage.messageTone(): MessageTone =
    when (type) {
        FeedbackType.BURNOUT_RISK,
        FeedbackType.RECOVERY_WARNING,
        FeedbackType.IDENTITY_CONFLICT -> MessageTone.WARNING
        FeedbackType.PUSH_GUIDANCE,
        FeedbackType.POSITIVE_STEADY_STATE -> MessageTone.INFO
    }
