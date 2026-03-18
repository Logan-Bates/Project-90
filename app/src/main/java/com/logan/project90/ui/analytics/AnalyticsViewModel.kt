package com.logan.project90.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.AnalyticsOverview
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

data class AnalyticsUiState(
    val overview: AnalyticsOverview = AnalyticsOverview(experiment = null)
) {
    val weightedMomentumDisplay: String
        get() = String.format(Locale.US, "%.1f", overview.weightedMomentum)
}

class AnalyticsViewModel(appContainer: AppContainer) : ViewModel() {
    val uiState: StateFlow<AnalyticsUiState> =
        appContainer.getAnalyticsOverviewUseCase(todayLocalDate())
            .map { overview -> AnalyticsUiState(overview = overview) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            simpleFactory { AnalyticsViewModel(appContainer) }
    }
}
