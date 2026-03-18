package com.logan.project90.ui.today

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.logan.project90.core.model.displayName
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.domain.model.FeedbackMessage
import com.logan.project90.domain.model.FeedbackType
import com.logan.project90.domain.model.TodayIdentityCardModel
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.SupportText
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayIdentityCard(
    card: TodayIdentityCardModel,
    isEditorOpen: Boolean,
    editorState: TodayLogEditorUiState,
    onOpenEditor: (Long) -> Unit,
    onCloseEditor: () -> Unit,
    onEffortChanged: (String) -> Unit,
    onStatusChanged: (IdentityStatus) -> Unit,
    onEnergyChanged: (String) -> Unit,
    onMoodChanged: (String) -> Unit,
    onResistanceChanged: (ResistanceLevel) -> Unit,
    onReflectionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onOpenIdentity: (Long) -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var resistanceExpanded by remember { mutableStateOf(false) }

    AppCard(onClick = { onOpenIdentity(card.identity.id) }) {
        androidx.compose.material3.Text(
            text = card.identity.name,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
        LabeledValue("Category", card.identity.category.displayName())
        if (card.todayLog == null) {
            SupportText(text = "Not logged today.")
        } else {
            LabeledValue("Saved status", card.todayLog.status.displayName())
            LabeledValue("Saved effort", "${card.todayLog.effortMinutes} min")
        }
        LabeledValue("Consistency Score", formatScore(card.analytics.strength14))
        LabeledValue("Momentum Score", formatScore(card.analytics.momentum))
        card.feedback?.let {
            InlineMessage(text = it.message, tone = it.messageTone())
        }
        TextButton(
            onClick = { onOpenIdentity(card.identity.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text(text = "See Details")
        }
        PrimaryButton(
            text = if (isEditorOpen) "Hide Editor" else if (card.todayLog == null) "Record Today" else "Update Today",
            onClick = {
                if (isEditorOpen) onCloseEditor() else onOpenEditor(card.identity.id)
            }
        )
        if (isEditorOpen) {
            NumberField(
                value = editorState.effortMinutes,
                onValueChange = onEffortChanged,
                label = "Effort minutes"
            )
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                OutlinedTextField(
                    value = editorState.status.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { androidx.compose.material3.Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    IdentityStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { androidx.compose.material3.Text(status.displayName()) },
                            onClick = {
                                onStatusChanged(status)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
            NumberField(value = editorState.energy, onValueChange = onEnergyChanged, label = "Energy (1-5)")
            NumberField(value = editorState.mood, onValueChange = onMoodChanged, label = "Mood (1-5)")
            ExposedDropdownMenuBox(
                expanded = resistanceExpanded,
                onExpandedChange = { resistanceExpanded = !resistanceExpanded }
            ) {
                OutlinedTextField(
                    value = editorState.resistance.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { androidx.compose.material3.Text("Resistance") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resistanceExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(expanded = resistanceExpanded, onDismissRequest = { resistanceExpanded = false }) {
                    ResistanceLevel.entries.forEach { resistance ->
                        DropdownMenuItem(
                            text = { androidx.compose.material3.Text(resistance.displayName()) },
                            onClick = {
                                onResistanceChanged(resistance)
                                resistanceExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = editorState.reflection,
                onValueChange = onReflectionChanged,
                label = { androidx.compose.material3.Text("Reflection") },
                modifier = Modifier.fillMaxWidth()
            )
            editorState.inputError?.let { InlineMessage(text = it, tone = MessageTone.ERROR) }
            editorState.saveWarning?.let { InlineMessage(text = it, tone = MessageTone.WARNING) }
            editorState.saveError?.let { InlineMessage(text = it, tone = MessageTone.ERROR) }
            PrimaryButton(
                text = if (card.todayLog == null) "Save Today" else "Update Today",
                onClick = onSave,
                enabled = editorState.canSave
            )
        }
    }
}

private fun formatScore(value: Double): String =
    String.format(Locale.US, "%.1f", value)

internal fun FeedbackMessage.messageTone(): MessageTone =
    when (type) {
        FeedbackType.BURNOUT_RISK,
        FeedbackType.RECOVERY_WARNING,
        FeedbackType.IDENTITY_CONFLICT -> MessageTone.WARNING
        FeedbackType.PUSH_GUIDANCE,
        FeedbackType.POSITIVE_STEADY_STATE -> MessageTone.INFO
    }
