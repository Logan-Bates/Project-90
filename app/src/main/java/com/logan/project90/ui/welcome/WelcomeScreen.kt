package com.logan.project90.ui.welcome

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.Identity
import com.logan.project90.ui.components.AppCard
import com.logan.project90.ui.components.AppScreen
import com.logan.project90.ui.components.PrimaryButton
import com.logan.project90.ui.components.ScreenIntro
import com.logan.project90.ui.components.SupportText
import com.logan.project90.ui.components.MessageTone
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
    AppScreen {
        ScreenIntro(
            title = "Project 90",
            subtitle = "Create one experiment, one identity, and log one day with PRD-backed scoring."
        )
        AppCard {
            SupportText(
                text = if (uiState.hasIdentity) "Your current slice is ready to continue."
                else "Build a focused 90-day system around consistent daily action.",
                tone = MessageTone.INFO
            )
            PrimaryButton(
                text = if (uiState.onboardingComplete) "Continue" else "Start",
                onClick = onContinue
            )
        }
    }
}

internal fun <T : ViewModel> simpleFactory(block: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = block() as VM
    }
