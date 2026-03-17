package com.logan.project90.ui.identity

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
import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateIdentityUiState(
    val experimentId: Long = 0,
    val name: String = "",
    val statement: String = "",
    val category: IdentityCategory = IdentityCategory.MIND,
    val floorMinutes: String = "20",
    val pushMinutes: String = "40",
    val importanceWeight: String = "2",
    val warning: String? = null,
    val error: String? = null
) {
    val floorValue: Int? get() = floorMinutes.toIntOrNull()
    val pushValue: Int? get() = pushMinutes.toIntOrNull()
    val weightValue: Int? get() = importanceWeight.toIntOrNull()
    val canSave: Boolean
        get() = name.isNotBlank() &&
            statement.isNotBlank() &&
            floorValue in 1..1440 &&
            pushValue in 1..1440 &&
            weightValue in 1..3
}

class CreateIdentityViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateIdentityUiState())
    val uiState: StateFlow<CreateIdentityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val experiment = appContainer.experimentRepository.getFirstExperiment()
            _uiState.value = _uiState.value.copy(experimentId = experiment?.id ?: 0)
        }
    }

    fun updateName(value: String) { _uiState.value = _uiState.value.copy(name = value, error = null) }
    fun updateStatement(value: String) { _uiState.value = _uiState.value.copy(statement = value, error = null) }
    fun updateCategory(value: IdentityCategory) { _uiState.value = _uiState.value.copy(category = value, error = null) }
    fun updateFloorMinutes(value: String) { _uiState.value = _uiState.value.copy(floorMinutes = value, error = null) }
    fun updatePushMinutes(value: String) { _uiState.value = _uiState.value.copy(pushMinutes = value, error = null) }
    fun updateWeight(value: String) { _uiState.value = _uiState.value.copy(importanceWeight = value, error = null) }

    fun saveIdentity(onSaved: () -> Unit) {
        val floorMinutes = _uiState.value.floorValue
        val pushMinutes = _uiState.value.pushValue
        val importanceWeight = _uiState.value.weightValue
        val validationError = when {
            floorMinutes == null || floorMinutes !in 1..1440 ->
                "Floor minutes must be between 1 and 1,440."
            pushMinutes == null || pushMinutes !in 1..1440 ->
                "Push minutes must be between 1 and 1,440."
            importanceWeight == null || importanceWeight !in 1..3 ->
                "Importance must be a whole number from 1 to 3."
            else -> null
        }
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            return
        }
        val safeFloorMinutes = floorMinutes ?: return
        val safePushMinutes = pushMinutes ?: return
        val safeImportanceWeight = importanceWeight ?: return

        viewModelScope.launch {
            val result = appContainer.createIdentityUseCase(
                experimentId = _uiState.value.experimentId,
                name = _uiState.value.name,
                statement = _uiState.value.statement,
                category = _uiState.value.category,
                floorMinutes = safeFloorMinutes,
                pushMinutes = safePushMinutes,
                importanceWeight = safeImportanceWeight,
                createdDate = todayLocalDate()
            )
            result
                .onSuccess { (_, warning) ->
                    _uiState.value = _uiState.value.copy(warning = warning, error = null)
                    onSaved()
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { CreateIdentityViewModel(appContainer) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIdentityScreen(
    uiState: CreateIdentityUiState,
    onNameChanged: (String) -> Unit,
    onStatementChanged: (String) -> Unit,
    onCategoryChanged: (IdentityCategory) -> Unit,
    onFloorChanged: (String) -> Unit,
    onPushChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Create Identity", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = { Text("Identity name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.statement,
            onValueChange = onStatementChanged,
            label = { Text("Identity statement") },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = uiState.category.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                IdentityCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategoryChanged(category)
                            expanded = false
                        }
                    )
                }
            }
        }
        NumberField(value = uiState.floorMinutes, onValueChange = onFloorChanged, label = "Floor minutes")
        NumberField(value = uiState.pushMinutes, onValueChange = onPushChanged, label = "Push minutes")
        NumberField(value = uiState.importanceWeight, onValueChange = onWeightChanged, label = "Importance (1-3)")
        uiState.warning?.let { Text(text = it, color = MaterialTheme.colorScheme.tertiary) }
        uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onSave,
            enabled = uiState.canSave
        ) {
            Text(text = "Save identity")
        }
    }
}
