package com.logan.project90.ui.today

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.core.util.formatDisplayDate
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.FeedbackMessage
import com.logan.project90.domain.model.TodaySlice
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayLogEditorUiState(
    val activeIdentityId: Long? = null,
    val hydratedIdentityId: Long? = null,
    val effortMinutes: String = "",
    val status: IdentityStatus = IdentityStatus.FLOOR_PROTECTED,
    val energy: String = "3",
    val mood: String = "3",
    val resistance: ResistanceLevel = ResistanceLevel.NONE,
    val reflection: String = "",
    val saveWarning: String? = null,
    val saveError: String? = null
) {
    val effortValue: Int? get() = effortMinutes.toIntOrNull()
    val energyValue: Int? get() = energy.toIntOrNull()
    val moodValue: Int? get() = mood.toIntOrNull()
    val inputError: String?
        get() = when {
            activeIdentityId == null -> null
            effortValue == null || effortValue !in 0..1440 -> ValidationMessages.effortRange0To1440
            energyValue == null || energyValue !in 1..5 -> ValidationMessages.range1To5
            moodValue == null || moodValue !in 1..5 -> ValidationMessages.range1To5
            else -> null
        }
    val canSave: Boolean
        get() = activeIdentityId != null && inputError == null
}

data class TodayUiState(
    val slice: TodaySlice = TodaySlice(null, null),
    val editor: TodayLogEditorUiState = TodayLogEditorUiState()
)

class TodayViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val inputs = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = combine(
        appContainer.getTodaySliceUseCase(todayLocalDate()),
        inputs
    ) { slice, draft ->
        val activeIdentityId = draft.editor.activeIdentityId
        if (activeIdentityId == null) {
            draft.copy(slice = slice)
        } else {
            val activeCard = slice.identityCards.firstOrNull { it.identity.id == activeIdentityId }
            when {
                activeCard == null -> draft.copy(slice = slice, editor = TodayLogEditorUiState())
                draft.editor.hydratedIdentityId != activeIdentityId -> {
                    val log = activeCard.todayLog
                    draft.copy(
                        slice = slice,
                        editor = draft.editor.copy(
                            hydratedIdentityId = activeIdentityId,
                            effortMinutes = log?.effortMinutes?.toString().orEmpty(),
                            status = log?.status ?: IdentityStatus.FLOOR_PROTECTED,
                            energy = log?.energy?.toString() ?: "3",
                            mood = log?.mood?.toString() ?: "3",
                            resistance = log?.resistance ?: ResistanceLevel.NONE,
                            reflection = log?.reflection.orEmpty(),
                            saveWarning = null,
                            saveError = null
                        )
                    )
                }
                else -> draft.copy(slice = slice)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun openEditor(identityId: Long) {
        inputs.value = inputs.value.copy(
            editor = TodayLogEditorUiState(activeIdentityId = identityId)
        )
    }

    fun closeEditor() {
        inputs.value = inputs.value.copy(editor = TodayLogEditorUiState())
    }

    fun updateEffortMinutes(value: String) {
        updateEditor { copy(effortMinutes = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun updateStatus(value: IdentityStatus) {
        updateEditor { copy(status = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun updateEnergy(value: String) {
        updateEditor { copy(energy = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun updateMood(value: String) {
        updateEditor { copy(mood = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun updateResistance(value: ResistanceLevel) {
        updateEditor { copy(resistance = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun updateReflection(value: String) {
        updateEditor { copy(reflection = value, hydratedIdentityId = activeIdentityId, saveError = null, saveWarning = null) }
    }

    fun saveLog() {
        val current = uiState.value
        val activeIdentityId = current.editor.activeIdentityId ?: run {
            updateEditor { copy(saveError = ValidationMessages.createIdentityBeforeLogging) }
            return
        }
        val card = current.slice.identityCards.firstOrNull { it.identity.id == activeIdentityId } ?: run {
            updateEditor { copy(saveError = ValidationMessages.createIdentityBeforeLogging) }
            return
        }
        val effortMinutes = current.editor.effortValue
        val energy = current.editor.energyValue
        val mood = current.editor.moodValue
        if (effortMinutes == null || effortMinutes !in 0..1440) {
            updateEditor { copy(saveError = ValidationMessages.effortRange0To1440, saveWarning = null) }
            return
        }
        if (energy == null || energy !in 1..5) {
            updateEditor { copy(saveError = ValidationMessages.range1To5, saveWarning = null) }
            return
        }
        if (mood == null || mood !in 1..5) {
            updateEditor { copy(saveError = ValidationMessages.range1To5, saveWarning = null) }
            return
        }
        viewModelScope.launch {
            val result = appContainer.logIdentityDayUseCase(
                identity = card.identity,
                logDate = todayLocalDate(),
                effortMinutes = effortMinutes,
                status = current.editor.status,
                energy = energy,
                mood = mood,
                resistance = current.editor.resistance,
                reflection = current.editor.reflection
            )
            result
                .onSuccess {
                    updateEditor { copy(saveWarning = it.warning, saveError = null, hydratedIdentityId = activeIdentityId) }
                }
                .onFailure {
                    updateEditor { copy(saveError = it.message, saveWarning = null, hydratedIdentityId = activeIdentityId) }
                }
        }
    }

    private fun updateEditor(transform: TodayLogEditorUiState.() -> TodayLogEditorUiState) {
        inputs.value = inputs.value.copy(editor = inputs.value.editor.transform())
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { TodayViewModel(appContainer) }
    }
}

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onOpenEditor: (Long) -> Unit,
    onCloseEditor: () -> Unit,
    onEffortChanged: (String) -> Unit,
    onStatusChanged: (IdentityStatus) -> Unit,
    onEnergyChanged: (String) -> Unit,
    onMoodChanged: (String) -> Unit,
    onResistanceChanged: (ResistanceLevel) -> Unit,
    onReflectionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onOpenIdentity: (Long) -> Unit,
    onManageIdentities: () -> Unit
) {
    AppScreen(scrollable = true) {
        ScreenIntro(
            title = "Today",
            subtitle = "Review the experiment, scan feedback, and log each identity from one dashboard."
        )
        ScreenSection(title = "Current Experiment") {
            LabeledValue("Date", formatDisplayDate(todayLocalDate()))
            LabeledValue("Current Experiment", uiState.slice.experiment?.name ?: "Not set up yet")
            LabeledValue("Identity Overview", uiState.slice.identityCards.size.toString())
            androidx.compose.material3.TextButton(onClick = onManageIdentities) {
                androidx.compose.material3.Text("Edit Identities")
            }
        }
        uiState.slice.experimentFeedback?.let { feedback ->
            ScreenSection(title = "What the pattern suggests") {
                androidx.compose.material3.Text(
                    text = feedback.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                )
                InlineMessage(text = feedback.message, tone = feedback.messageTone())
            }
        }
        if (uiState.slice.identityCards.isEmpty()) {
            ScreenSection(title = "Identity Overview") {
                SupportText(text = "Add an identity to start logging daily progress.")
                androidx.compose.material3.TextButton(onClick = onManageIdentities) {
                    androidx.compose.material3.Text("Edit Identities")
                }
            }
        } else {
            uiState.slice.identityCards.forEach { card ->
                TodayIdentityCard(
                    card = card,
                    isEditorOpen = uiState.editor.activeIdentityId == card.identity.id,
                    editorState = uiState.editor,
                    onOpenEditor = onOpenEditor,
                    onCloseEditor = onCloseEditor,
                    onEffortChanged = onEffortChanged,
                    onStatusChanged = onStatusChanged,
                    onEnergyChanged = onEnergyChanged,
                    onMoodChanged = onMoodChanged,
                    onResistanceChanged = onResistanceChanged,
                    onReflectionChanged = onReflectionChanged,
                    onSave = onSave,
                    onOpenIdentity = onOpenIdentity
                )
            }
        }
    }
}
