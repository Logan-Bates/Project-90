package com.logan.project90.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.core.util.formatDisplayDate
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.TodaySlice
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class TodayUiState(
    val slice: TodaySlice = TodaySlice(null, null, null, null),
    val effortMinutes: String = "",
    val status: IdentityStatus = IdentityStatus.FLOOR_PROTECTED,
    val energy: String = "3",
    val mood: String = "3",
    val resistance: ResistanceLevel = ResistanceLevel.NONE,
    val reflection: String = "",
    val didHydrateFromSavedLog: Boolean = false,
    val saveWarning: String? = null,
    val saveError: String? = null
) {
    val effortValue: Int? get() = effortMinutes.toIntOrNull()
    val energyValue: Int? get() = energy.toIntOrNull()
    val moodValue: Int? get() = mood.toIntOrNull()
    val inputError: String?
        get() = when {
            effortValue == null || effortValue !in 0..1440 ->
                "Effort minutes must be between 0 and 1,440."
            energyValue == null || energyValue !in 1..5 ->
                "Energy must be a whole number from 1 to 5."
            moodValue == null || moodValue !in 1..5 ->
                "Mood must be a whole number from 1 to 5."
            else -> null
        }
    val canSave: Boolean
        get() = slice.identity != null && inputError == null
}

class TodayViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val inputs = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = combine(
        appContainer.getTodaySliceUseCase(todayLocalDate()),
        inputs
    ) { slice, draft ->
        val log = slice.todayLog
        if (log != null && !draft.didHydrateFromSavedLog) {
            draft.copy(
                slice = slice,
                effortMinutes = log.effortMinutes.toString(),
                status = log.status,
                energy = log.energy.toString(),
                mood = log.mood.toString(),
                resistance = log.resistance,
                reflection = log.reflection,
                didHydrateFromSavedLog = true
            )
        } else {
            draft.copy(slice = slice)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun updateEffortMinutes(value: String) {
        inputs.value = inputs.value.copy(effortMinutes = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }
    fun updateStatus(value: IdentityStatus) {
        inputs.value = inputs.value.copy(status = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }
    fun updateEnergy(value: String) {
        inputs.value = inputs.value.copy(energy = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }
    fun updateMood(value: String) {
        inputs.value = inputs.value.copy(mood = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }
    fun updateResistance(value: ResistanceLevel) {
        inputs.value = inputs.value.copy(resistance = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }
    fun updateReflection(value: String) {
        inputs.value = inputs.value.copy(reflection = value, didHydrateFromSavedLog = true, saveError = null, saveWarning = null)
    }

    fun saveLog() {
        val current = uiState.value
        val identity = current.slice.identity ?: run {
            inputs.value = inputs.value.copy(saveError = "Create an identity before logging.")
            return
        }
        val effortMinutes = current.effortValue
        val energy = current.energyValue
        val mood = current.moodValue
        if (effortMinutes == null || effortMinutes !in 0..1440) {
            inputs.value = inputs.value.copy(saveError = "Effort minutes must be between 0 and 1,440.", saveWarning = null)
            return
        }
        if (energy == null || energy !in 1..5) {
            inputs.value = inputs.value.copy(saveError = "Energy must be a whole number from 1 to 5.", saveWarning = null)
            return
        }
        if (mood == null || mood !in 1..5) {
            inputs.value = inputs.value.copy(saveError = "Mood must be a whole number from 1 to 5.", saveWarning = null)
            return
        }
        viewModelScope.launch {
            val result = appContainer.logIdentityDayUseCase(
                identity = identity,
                logDate = todayLocalDate(),
                effortMinutes = effortMinutes,
                status = current.status,
                energy = energy,
                mood = mood,
                resistance = current.resistance,
                reflection = current.reflection
            )
            result
                .onSuccess { inputs.value = inputs.value.copy(saveWarning = it.warning, saveError = null) }
                .onFailure { inputs.value = inputs.value.copy(saveError = it.message, saveWarning = null) }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { TodayViewModel(appContainer) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onEffortChanged: (String) -> Unit,
    onStatusChanged: (IdentityStatus) -> Unit,
    onEnergyChanged: (String) -> Unit,
    onMoodChanged: (String) -> Unit,
    onResistanceChanged: (ResistanceLevel) -> Unit,
    onReflectionChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var resistanceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Today", style = MaterialTheme.typography.headlineSmall)
        ScreenSection(title = "Active Slice") {
            LabeledValue("Date", formatDisplayDate(todayLocalDate()))
            LabeledValue("Experiment", uiState.slice.experiment?.name ?: "Not created")
            LabeledValue("Identity", uiState.slice.identity?.name ?: "Not created")
        }
        ScreenSection(title = "Metrics") {
            LabeledValue("Strength14", formatScore(uiState.slice.analytics?.strength14))
            LabeledValue("Momentum", formatScore(uiState.slice.analytics?.momentum))
            LabeledValue("Strength7", formatScore(uiState.slice.analytics?.strength7))
            LabeledValue("PushFreq14", formatScore(uiState.slice.analytics?.pushFreq14))
            LabeledValue("RecoveryBalance14", formatScore(uiState.slice.analytics?.recoveryBalance14))
        }
        ScreenSection(title = "Log Today") {
            NumberField(value = uiState.effortMinutes, onValueChange = onEffortChanged, label = "Effort minutes")
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                OutlinedTextField(
                    value = uiState.status.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    IdentityStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                onStatusChanged(status)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
            NumberField(value = uiState.energy, onValueChange = onEnergyChanged, label = "Energy (1-5)")
            NumberField(value = uiState.mood, onValueChange = onMoodChanged, label = "Mood (1-5)")
            ExposedDropdownMenuBox(
                expanded = resistanceExpanded,
                onExpandedChange = { resistanceExpanded = !resistanceExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.resistance.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resistance") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resistanceExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(
                    expanded = resistanceExpanded,
                    onDismissRequest = { resistanceExpanded = false }
                ) {
                    ResistanceLevel.entries.forEach { resistance ->
                        DropdownMenuItem(
                            text = { Text(resistance.name) },
                            onClick = {
                                onResistanceChanged(resistance)
                                resistanceExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = uiState.reflection,
                onValueChange = onReflectionChanged,
                label = { Text("Reflection") },
                modifier = Modifier.fillMaxWidth()
            )
            uiState.inputError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            uiState.saveWarning?.let { Text(text = it, color = MaterialTheme.colorScheme.tertiary) }
            uiState.saveError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSave, enabled = uiState.canSave) {
                Text(text = "Save today")
            }
        }
    }
}

private fun formatScore(value: Double?): String =
    if (value == null) "--" else String.format(Locale.US, "%.1f", value)
