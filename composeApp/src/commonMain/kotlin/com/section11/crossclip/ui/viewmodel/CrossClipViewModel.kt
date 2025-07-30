package com.section11.crossclip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.domain.models.User
import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.DismissAddStringScreen
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnAddStringTapped
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnDeleteString
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnRefreshSharedStrings
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnSignIn
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnSignOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: SharedStringsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                    if (user != null) {
                        refreshSharedStrings()
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun onUiEvent(mainUiEvents: MainUiEvents) {
        when(mainUiEvents) {
            is OnSignIn -> signInWithGoogle()
            is OnSignOut -> signOut()
            is OnDeleteString -> deleteSharedString(mainUiEvents.deletedString)
            is OnRefreshSharedStrings -> refreshSharedStrings()
            is OnAddStringTapped -> handleAddStringTapped()
            is DismissAddStringScreen -> {
                _uiState.value = _uiState.value.copy(showAddScreen = false)
                refreshSharedStrings()
            }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.signInWithGoogle()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                    refreshSharedStrings()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            repository.signOut()
                .onSuccess {
                    _uiState.value = MainUiState()
                }
        }
    }

    private fun refreshSharedStrings() {
        val user = _uiState.value.user ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getSharedStrings(user.id)
                .onSuccess { strings ->
                    _uiState.value = _uiState.value.copy(
                        sharedStrings = strings.sortedByDescending { it.timestamp },
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                        retryAction = { refreshSharedStrings() }
                    )
                }
        }
    }

    private fun deleteSharedString(id: String) {
        viewModelScope.launch {
            repository.deleteSharedString(id)
                .onSuccess {
                    refreshSharedStrings()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    private fun handleAddStringTapped() {
        _uiState.value = _uiState.value.copy(showAddScreen = true)
    }

    sealed class MainUiEvents {
        data object OnSignOut : MainUiEvents()
        data object OnSignIn : MainUiEvents()
        data object OnRefreshSharedStrings : MainUiEvents()
        data object OnAddStringTapped : MainUiEvents()
        data class OnDeleteString(val deletedString: String) : MainUiEvents()
        data object DismissAddStringScreen : MainUiEvents()
    }

    data class MainUiState(
        val user: User? = null,
        val sharedStrings: List<SharedString> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val retryAction: (() -> Unit)? = null,
        val showAddScreen: Boolean = false
    )
}
