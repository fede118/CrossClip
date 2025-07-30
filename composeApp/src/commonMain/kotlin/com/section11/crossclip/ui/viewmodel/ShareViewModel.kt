package com.section11.crossclip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.framework.utils.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ShareViewModel(private val repository: SharedStringsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(textToShare = text)
    }

    fun saveSharedString() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val user = repository.getCurrentUser().getOrNull()
            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please sign in first"
                )
                return@launch
            }

            val sharedString = SharedString(
                content = _uiState.value.textToShare,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                userId = user.id,
                deviceInfo = DeviceInfo.getDeviceInfo()
            )

            repository.addSharedString(sharedString)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, onDismiss = true)
                    //todo: show snackbar
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun onCancel() {
        _uiState.value = _uiState.value.copy(onDismiss = true)
    }

    data class ShareUiState(
        val textToShare: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val onDismiss: Boolean = false
    )
}
