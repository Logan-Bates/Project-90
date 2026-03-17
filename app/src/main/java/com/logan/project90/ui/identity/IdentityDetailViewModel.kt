package com.logan.project90.ui.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.logan.project90.core.util.todayLocalDate
import com.logan.project90.di.AppContainer
import com.logan.project90.domain.model.IdentityDetail
import com.logan.project90.ui.welcome.simpleFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IdentityDetailUiState(
    val detail: IdentityDetail? = null,
    val error: String? = null
)

class IdentityDetailViewModel(
    appContainer: AppContainer,
    identityId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdentityDetailUiState())
    val uiState: StateFlow<IdentityDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val detail = appContainer.getIdentityDetailUseCase(identityId, todayLocalDate())
            _uiState.value = if (detail == null) {
                IdentityDetailUiState(error = "Identity not found.")
            } else {
                IdentityDetailUiState(detail = detail)
            }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer, identityId: Long): ViewModelProvider.Factory =
            simpleFactory { IdentityDetailViewModel(appContainer, identityId) }
    }
}
