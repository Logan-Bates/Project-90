package com.logan.project90.ui.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.unit.dp
import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.displayName
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.preset.IdentityPreset
import com.logan.project90.domain.preset.IdentityPresets
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.SupportText
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PresetSelectionUiState(
    val availableCategories: List<IdentityCategory> = IdentityCategory.entries,
    val availablePresets: List<IdentityPreset> = IdentityPresets.all,
    val identityCount: Int = 0
) {
    val canAddMore: Boolean get() = identityCount < 4 && availableCategories.isNotEmpty()
    val maxMessage: String? get() = if (identityCount >= 4) ValidationMessages.maxFourIdentities else null
}

class PresetSelectionViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(PresetSelectionUiState())
    val uiState: StateFlow<PresetSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val experiment = appContainer.experimentRepository.getFirstExperiment()
            val experimentId = experiment?.id ?: 0L
            val identities = if (experimentId == 0L) emptyList() else appContainer.identityRepository.getIdentitiesForExperiment(experimentId)
            val usedCategories = identities.map { it.category }.toSet()
            val availableCategories = IdentityCategory.entries.filterNot { it in usedCategories }
            _uiState.value = PresetSelectionUiState(
                availableCategories = availableCategories,
                availablePresets = IdentityPresets.all.filter { it.category in availableCategories },
                identityCount = identities.size
            )
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { PresetSelectionViewModel(appContainer) }
    }
}

@Composable
fun PresetSelectionScreen(
    uiState: PresetSelectionUiState,
    onPresetSelected: (String) -> Unit,
    onCreateCustom: () -> Unit
) {
    AppScreen(scrollable = true) {
        ScreenIntro(
            title = "Choose a Starting Point",
            subtitle = "Pick a preset to prefill the identity form, or create a custom identity."
        )
        if (uiState.canAddMore) {
            PrimaryButton(text = "Create Your Own", onClick = onCreateCustom)
        }
        uiState.maxMessage?.let {
            InlineMessage(text = it, tone = MessageTone.WARNING)
        }
        if (uiState.availablePresets.isEmpty() && uiState.canAddMore) {
            SupportText(text = "No preset categories are available. Create a custom identity instead.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.availablePresets.forEach { preset ->
                    PresetCard(preset = preset, onSelect = { onPresetSelected(preset.id) })
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: IdentityPreset,
    onSelect: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onSelect) {
        androidx.compose.material3.Text(
            text = preset.name,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
        SupportText(text = preset.category.displayName())
        androidx.compose.material3.Text(
            text = preset.description,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
        SupportText(
            text = "Floor ${preset.defaultFloorMinutes} min · Push ${preset.defaultPushMinutes} min · Weight ${preset.defaultWeight}"
        )
    }
}
