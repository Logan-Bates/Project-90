package com.logan.project90.ui.identity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.preset.IdentityPresets
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.NumberField
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateIdentityUiState(
    val identityId: Long? = null,
    val isEditMode: Boolean = false,
    val experimentId: Long = 0,
    val name: String = "",
    val statement: String = "",
    val category: IdentityCategory? = IdentityCategory.MIND,
    val availableCategories: List<IdentityCategory> = IdentityCategory.entries,
    val identityCount: Int = 0,
    val floorMinutes: String = "20",
    val pushMinutes: String = "40",
    val importanceWeight: String = "2",
    val warning: String? = null,
    val error: String? = null
) {
    val floorValue: Int? get() = floorMinutes.toIntOrNull()
    val pushValue: Int? get() = pushMinutes.toIntOrNull()
    val weightValue: Int? get() = importanceWeight.toIntOrNull()
    val canAddMore: Boolean get() = identityCount < 4 && availableCategories.isNotEmpty()
    val inputError: String?
        get() = when {
            !isEditMode && !canAddMore -> ValidationMessages.maxFourIdentities
            name.isBlank() -> ValidationMessages.identityNameRequired
            name.trim().length > 50 -> ValidationMessages.identityNameTooLong
            statement.isBlank() -> ValidationMessages.identityStatementRequired
            statement.trim().length > 160 -> ValidationMessages.identityStatementTooLong
            category == null -> ValidationMessages.oneIdentityPerCategory
            floorValue == null || floorValue !in 1..1440 -> ValidationMessages.minutesRange1To1440
            pushValue == null || pushValue !in 1..1440 -> ValidationMessages.minutesRange1To1440
            weightValue == null || weightValue !in 1..3 -> ValidationMessages.range1To3
            else -> null
        }
    val canSave: Boolean
        get() = inputError == null
}

class CreateIdentityViewModel(
    private val appContainer: AppContainer,
    presetId: String?,
    private val identityId: Long?
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateIdentityUiState())
    val uiState: StateFlow<CreateIdentityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (editingIdentity, availableCategories) = appContainer.getEditableIdentityUseCase(identityId)
            val experimentId = editingIdentity?.experimentId
                ?: appContainer.experimentRepository.getFirstExperiment()?.id
                ?: 0L
            val identityCount = if (experimentId == 0L) 0 else appContainer.identityRepository.getIdentityCountForExperiment(experimentId)
            _uiState.value = _uiState.value.copy(
                identityId = editingIdentity?.id,
                isEditMode = editingIdentity != null,
                experimentId = experimentId,
                identityCount = identityCount,
                availableCategories = availableCategories,
                category = editingIdentity?.category ?: availableCategories.firstOrNull(),
                name = editingIdentity?.name ?: _uiState.value.name,
                statement = editingIdentity?.statement ?: _uiState.value.statement,
                floorMinutes = editingIdentity?.floorMinutes?.toString() ?: _uiState.value.floorMinutes,
                pushMinutes = editingIdentity?.pushMinutes?.toString() ?: _uiState.value.pushMinutes,
                importanceWeight = editingIdentity?.importanceWeight?.toString() ?: _uiState.value.importanceWeight
            )
            if (editingIdentity == null) {
                applyPreset(presetId)
            }
        }
    }

    private fun applyPreset(presetId: String?) {
        val preset = IdentityPresets.findById(presetId) ?: return
        val current = _uiState.value
        if (preset.category !in current.availableCategories) return
        _uiState.value = current.copy(
            name = preset.name,
            statement = preset.defaultStatement,
            category = preset.category,
            floorMinutes = preset.defaultFloorMinutes.toString(),
            pushMinutes = preset.defaultPushMinutes.toString(),
            importanceWeight = preset.defaultWeight.toString(),
            error = null,
            warning = null
        )
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value.take(50), error = null)
    }

    fun updateStatement(value: String) {
        _uiState.value = _uiState.value.copy(statement = value.take(160), error = null)
    }

    fun updateCategory(value: IdentityCategory) {
        if (value !in _uiState.value.availableCategories) return
        _uiState.value = _uiState.value.copy(category = value, error = null)
    }

    fun updateFloorMinutes(value: String) {
        _uiState.value = _uiState.value.copy(floorMinutes = value, error = null)
    }

    fun updatePushMinutes(value: String) {
        _uiState.value = _uiState.value.copy(pushMinutes = value, error = null)
    }

    fun updateWeight(value: String) {
        _uiState.value = _uiState.value.copy(importanceWeight = value, error = null)
    }

    fun saveIdentity(onSaved: () -> Unit) {
        val current = _uiState.value
        val floorMinutes = current.floorValue
        val pushMinutes = current.pushValue
        val importanceWeight = current.weightValue
        val category = current.category
        val validationError = when {
            !current.isEditMode && !current.canAddMore -> ValidationMessages.maxFourIdentities
            current.name.trim().length > 50 -> ValidationMessages.identityNameTooLong
            current.statement.trim().length > 160 -> ValidationMessages.identityStatementTooLong
            category == null -> ValidationMessages.oneIdentityPerCategory
            floorMinutes == null || floorMinutes !in 1..1440 -> ValidationMessages.minutesRange1To1440
            pushMinutes == null || pushMinutes !in 1..1440 -> ValidationMessages.minutesRange1To1440
            importanceWeight == null || importanceWeight !in 1..3 -> ValidationMessages.range1To3
            else -> null
        }
        if (validationError != null) {
            _uiState.value = current.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            val result = if (current.isEditMode) {
                appContainer.updateIdentityUseCase(
                    identityId = current.identityId ?: return@launch,
                    name = current.name,
                    statement = current.statement,
                    category = category ?: return@launch,
                    floorMinutes = floorMinutes ?: return@launch,
                    pushMinutes = pushMinutes ?: return@launch,
                    importanceWeight = importanceWeight ?: return@launch
                )
            } else {
                appContainer.createIdentityUseCase(
                    experimentId = current.experimentId,
                    name = current.name,
                    statement = current.statement,
                    category = category ?: return@launch,
                    floorMinutes = floorMinutes ?: return@launch,
                    pushMinutes = pushMinutes ?: return@launch,
                    importanceWeight = importanceWeight ?: return@launch,
                    createdDate = todayLocalDate()
                ).map { it.second }
            }
            result
                .onSuccess { warning ->
                    _uiState.value = _uiState.value.copy(warning = warning, error = null)
                    onSaved()
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer, presetId: String?, identityId: Long?): ViewModelProvider.Factory =
            simpleFactory { CreateIdentityViewModel(appContainer, presetId, identityId) }
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

    AppScreen(scrollable = true) {
        ScreenIntro(
            title = if (uiState.isEditMode) "Edit Identity" else "Create Identity",
            subtitle = if (uiState.isEditMode) {
                "Update the identity while keeping category rules and daily targets intact."
            } else {
                "Define one of up to four identities for this experiment."
            }
        )
        ScreenSection(title = "Identity Details") {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { androidx.compose.material3.Text("Identity name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    androidx.compose.material3.Text("${uiState.name.length}/50")
                }
            )
            OutlinedTextField(
                value = uiState.statement,
                onValueChange = onStatementChanged,
                label = { androidx.compose.material3.Text("Identity statement") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    androidx.compose.material3.Text("${uiState.statement.length}/160")
                }
            )
            if (uiState.availableCategories.size > 1 && uiState.category != null) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = uiState.category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { androidx.compose.material3.Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text(category.name) },
                                onClick = {
                                    onCategoryChanged(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.category?.name ?: "--",
                    onValueChange = {},
                    readOnly = true,
                    label = { androidx.compose.material3.Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SupportText(text = "Used categories are hidden so each experiment keeps one identity per category.")
        }
        ScreenSection(title = "Daily Targets") {
            NumberField(value = uiState.floorMinutes, onValueChange = onFloorChanged, label = "Floor minutes")
            NumberField(value = uiState.pushMinutes, onValueChange = onPushChanged, label = "Push minutes")
            NumberField(value = uiState.importanceWeight, onValueChange = onWeightChanged, label = "Importance (1-3)")
            uiState.warning?.let { InlineMessage(text = it, tone = MessageTone.WARNING) }
            (uiState.error ?: uiState.inputError)?.let { InlineMessage(text = it, tone = MessageTone.ERROR) }
            PrimaryButton(
                text = if (uiState.isEditMode) "Save changes" else "Save identity",
                onClick = onSave,
                enabled = uiState.canSave
            )
        }
    }
}
