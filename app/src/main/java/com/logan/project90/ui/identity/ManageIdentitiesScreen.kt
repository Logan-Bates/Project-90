package com.logan.project90.ui.identity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.Identity
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.InlineMessage
import com.logan.project90.ui.components.LabeledValue
import com.logan.project90.ui.components.MessageTone
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.ScreenSection
import com.logan.project90.ui.components.SupportText
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManageIdentitiesUiState(
    val experiment: Experiment? = null,
    val identities: List<Identity> = emptyList(),
    val error: String? = null
) {
    val canAddMore: Boolean get() = identities.size < 4
    val maxMessage: String? get() = if (identities.size >= 4) ValidationMessages.maxFourIdentities else null
}

class ManageIdentitiesViewModel(private val appContainer: AppContainer) : ViewModel() {
    private val actionState = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val identitiesFlow =
        appContainer.experimentRepository.observeFirstExperiment().flatMapLatest { experiment ->
            if (experiment == null) {
                flowOf(emptyList<Identity>())
            } else {
                appContainer.identityRepository.observeIdentitiesForExperiment(experiment.id)
            }
        }

    val uiState: StateFlow<ManageIdentitiesUiState> =
        combine(
            appContainer.experimentRepository.observeFirstExperiment(),
            identitiesFlow,
            actionState
        ) { experiment, identities, error ->
            ManageIdentitiesUiState(
                experiment = experiment,
                identities = identities,
                error = error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageIdentitiesUiState())

    fun deleteIdentity(identityId: Long) {
        viewModelScope.launch {
            appContainer.deleteIdentityUseCase(identityId)
                .onSuccess { actionState.value = null }
                .onFailure { actionState.value = it.message }
        }
    }

    fun clearError() {
        actionState.value = null
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { ManageIdentitiesViewModel(appContainer) }
    }
}

@Composable
fun ManageIdentitiesScreen(
    uiState: ManageIdentitiesUiState,
    onAddIdentity: () -> Unit,
    onEditIdentity: (Long) -> Unit,
    onDeleteIdentity: (Long) -> Unit,
    onClearError: () -> Unit,
    onContinue: () -> Unit
) {
    var pendingDelete by remember { mutableStateOf<Identity?>(null) }
    val deleteTarget = pendingDelete

    if (deleteTarget != null) {
        DeleteIdentityDialog(
            identity = deleteTarget,
            onConfirm = {
                onDeleteIdentity(deleteTarget.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    AppScreen(scrollable = true) {
        ScreenIntro(
            title = "Manage Identities",
            subtitle = "Build the experiment with one to four identities, one per category."
        )
        ScreenSection(title = "Experiment") {
            LabeledValue("Experiment", uiState.experiment?.name ?: "Not created")
            LabeledValue("Identities", "${uiState.identities.size} / 4")
        }
        ScreenSection(title = "Current Identities") {
            if (uiState.identities.isEmpty()) {
                SupportText(text = "No identities yet. Add one to start the daily loop.")
            } else {
                uiState.identities.forEach { identity ->
                    AppCard {
                        LabeledValue("Name", identity.name)
                        LabeledValue("Category", identity.category.name)
                        LabeledValue("Floor", "${identity.floorMinutes} min")
                        LabeledValue("Push", "${identity.pushMinutes} min")
                        androidx.compose.material3.TextButton(
                            onClick = { onEditIdentity(identity.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Text("Edit Identity")
                        }
                        androidx.compose.material3.TextButton(
                            onClick = { pendingDelete = identity },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Text("Delete Identity")
                        }
                    }
                }
            }
            uiState.error?.let {
                InlineMessage(text = it, tone = MessageTone.ERROR)
                androidx.compose.material3.TextButton(onClick = onClearError) {
                    androidx.compose.material3.Text("Dismiss")
                }
            }
            uiState.maxMessage?.let {
                InlineMessage(text = it, tone = MessageTone.WARNING)
            }
            if (uiState.canAddMore) {
                PrimaryButton(text = "Add Identity", onClick = onAddIdentity)
            }
        }
        PrimaryButton(
            text = "Continue to Today",
            onClick = onContinue,
            enabled = uiState.identities.isNotEmpty()
        )
    }
}
