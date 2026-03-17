package com.logan.project90.ui.experiment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.formatDisplayDate
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Create Experiment", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = { Text("Experiment name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(text = "Start date: ${formatDisplayDate(uiState.startDate)}")
        Text(text = "End date: ${formatDisplayDate(uiState.calculatedEndDate)}")
        uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onCreate, enabled = uiState.name.isNotBlank()) {
            Text(text = "Create 90-day experiment")
        }
    }
}
