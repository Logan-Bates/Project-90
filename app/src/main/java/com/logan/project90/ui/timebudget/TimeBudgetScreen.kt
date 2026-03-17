package com.logan.project90.ui.timebudget

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimeBudgetUiState(
    val minutes: String = "120",
    val error: String? = null
) {
    val inputError: String?
        get() = if (minutes.toIntOrNull() in 1..1440) null else ValidationMessages.minutesRange1To1440
}

class TimeBudgetViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(TimeBudgetUiState())
    val uiState: StateFlow<TimeBudgetUiState> = _uiState.asStateFlow()

    fun updateMinutes(value: String) {
        _uiState.value = _uiState.value.copy(minutes = value, error = null)
    }

    fun save(onSaved: () -> Unit) {
        val minutes = _uiState.value.minutes.toIntOrNull()
        if (minutes == null || minutes !in 1..1440) {
            _uiState.value = _uiState.value.copy(
                error = ValidationMessages.minutesRange1To1440
            )
            return
        }
        viewModelScope.launch {
            appContainer.completeOnboardingUseCase(minutes)
            onSaved()
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { TimeBudgetViewModel(appContainer) }
    }
}

@Composable
fun TimeBudgetScreen(
    uiState: TimeBudgetUiState,
    onMinutesChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    AppScreen {
        ScreenIntro(
            title = "Time Budget",
            subtitle = "Set your discretionary minutes per day for planning guardrails."
        )
        ScreenSection(title = "Daily Capacity") {
            NumberField(value = uiState.minutes, onValueChange = onMinutesChanged, label = "Minutes per day")
            (uiState.error ?: uiState.inputError)?.let {
                InlineMessage(text = it, tone = MessageTone.ERROR)
            }
            PrimaryButton(
                text = "Save time budget",
                onClick = onSave,
                enabled = uiState.inputError == null
            )
        }
    }
}
