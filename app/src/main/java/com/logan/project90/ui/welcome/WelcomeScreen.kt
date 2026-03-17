package com.logan.project90.ui.welcome

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
import com.logan.project90.domain.model.Identity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class WelcomeUiState(
    val onboardingComplete: Boolean = false,
    val hasExperiment: Boolean = false,
    val hasIdentity: Boolean = false
)

class WelcomeViewModel(appContainer: AppContainer) : ViewModel() {
    private val identityFlow =
        appContainer.experimentRepository.observeFirstExperiment().flatMapLatest { experiment ->
            if (experiment == null) {
                flowOf<Identity?>(null)
            } else {
                appContainer.identityRepository.observeFirstIdentityForExperiment(experiment.id)
            }
        }

    val uiState: StateFlow<WelcomeUiState> =
        combine(
            appContainer.settingsRepository.onboardingComplete,
            appContainer.experimentRepository.observeFirstExperiment(),
            identityFlow
        ) { onboardingComplete, experiment, identity ->
            WelcomeUiState(
                onboardingComplete = onboardingComplete,
                hasExperiment = experiment != null,
                hasIdentity = identity != null
            )
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WelcomeUiState())

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { WelcomeViewModel(appContainer) }
    }
}

@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Project 90", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Create one experiment, one identity, and log one day with PRD-backed scoring.")
        Button(onClick = onContinue) {
            Text(if (uiState.onboardingComplete) "Continue" else "Start")
        }
    }
}

internal fun <T : ViewModel> simpleFactory(block: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = block() as VM
    }
