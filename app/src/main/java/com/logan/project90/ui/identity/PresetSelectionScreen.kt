package com.logan.project90.ui.identity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.logan.project90.domain.preset.IdentityPreset
import com.logan.project90.domain.preset.IdentityPresets
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.SupportText

@Composable
fun PresetSelectionScreen(
    onPresetSelected: (String) -> Unit,
    onCreateCustom: () -> Unit
) {
    AppScreen(scrollable = true) {
        ScreenIntro(
            title = "Choose a Starting Point",
            subtitle = "Pick a preset to prefill the identity form, or create a custom identity."
        )
        PrimaryButton(text = "Create Custom", onClick = onCreateCustom)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IdentityPresets.all.forEach { preset ->
                PresetCard(preset = preset, onSelect = { onPresetSelected(preset.id) })
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
        androidx.compose.material3.Text(text = preset.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        SupportText(text = preset.category.name.lowercase().replaceFirstChar { it.uppercase() })
        androidx.compose.material3.Text(text = preset.description, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        SupportText(text = "Floor ${preset.defaultFloorMinutes} min · Push ${preset.defaultPushMinutes} min · Weight ${preset.defaultWeight}")
    }
}
