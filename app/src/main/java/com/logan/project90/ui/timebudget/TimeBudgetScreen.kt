package com.logan.project90.ui.timebudget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimeBudgetUiState(
    val minutes: String = "120",
    val error: String? = null
)

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
                error = "Discretionary minutes must be between 1 and 1,440."
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Time Budget", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Set your discretionary minutes per day for burnout warnings.")
        NumberField(value = uiState.minutes, onValueChange = onMinutesChanged, label = "Minutes per day")
        uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onSave, enabled = uiState.minutes.toIntOrNull() in 1..1440) {
            Text(text = "Save time budget")
        }
    }
}
