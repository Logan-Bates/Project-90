package com.logan.project90.ui.experiment

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.formatDisplayDate
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CreateExperimentUiState(
    val name: String = "",
    val startDate: LocalDate = todayLocalDate(),
    val durationDays: Int = 90,
    val error: String? = null
) {
    val calculatedEndDate: LocalDate = startDate.plusDays(durationDays.toLong() - 1)
}

class CreateExperimentViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateExperimentUiState())
    val uiState: StateFlow<CreateExperimentUiState> = _uiState.asStateFlow()

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value, error = null)
    }

    fun createExperiment(onCreated: () -> Unit) {
        viewModelScope.launch {
            val result = appContainer.createExperimentUseCase(
                name = _uiState.value.name,
                startDate = _uiState.value.startDate,
                durationDays = _uiState.value.durationDays
            )
            result
                .onSuccess { onCreated() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { CreateExperimentViewModel(appContainer) }
    }
}

@Composable
fun CreateExperimentScreen(
    uiState: CreateExperimentUiState,
    onNameChanged: (String) -> Unit,
    onCreate: () -> Unit
) {
    AppScreen {
        ScreenIntro(
            title = "Create Experiment",
            subtitle = "Name the 90-day experiment you want to run."
        )
        ScreenSection(title = "Experiment Setup") {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { androidx.compose.material3.Text("Experiment name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            LabeledValue("Start date", formatDisplayDate(uiState.startDate))
            LabeledValue("End date", formatDisplayDate(uiState.calculatedEndDate))
            uiState.error?.let { InlineMessage(text = it, tone = MessageTone.ERROR) }
            PrimaryButton(
                text = "Create 90-day experiment",
                onClick = onCreate,
                enabled = uiState.name.isNotBlank()
            )
        }
    }
}
