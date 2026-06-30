package com.sanjeeb.notiondrop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanjeeb.notiondrop.data.remote.StructuredContent
import com.sanjeeb.notiondrop.repository.ContentRepository
import com.sanjeeb.notiondrop.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Review(val content: StructuredContent) : MainUiState()
    object Success : MainUiState()
    data class Error(val message: String) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState

    private val _databases = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val databases: StateFlow<List<Pair<String, String>>> = _databases

    private val _selectedDatabaseId = MutableStateFlow<String?>(null)
    val selectedDatabaseId: StateFlow<String?> = _selectedDatabaseId

    init {
        loadDatabases()
    }

    fun loadDatabases() {
        val dbs = settingsRepository.getNotionDatabases()
        _databases.value = dbs
        if (dbs.isNotEmpty() && _selectedDatabaseId.value == null) {
            _selectedDatabaseId.value = dbs.first().second
        }
    }

    fun selectDatabase(id: String?) {
        _selectedDatabaseId.value = id
    }

    fun processContent(text: String, isAiExpanded: Boolean = true) {
        if (text.isBlank()) return
        _uiState.value = MainUiState.Loading

        viewModelScope.launch {
            try {
                val structuredContent = contentRepository.processContent(text, isAiExpanded, _selectedDatabaseId.value)
                if (settingsRepository.getReviewEnabled()) {
                    _uiState.value = MainUiState.Review(structuredContent)
                } else {
                    sendToNotion(structuredContent)
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Failed to process content")
            }
        }
    }

    fun sendToNotion(content: StructuredContent) {
        _uiState.value = MainUiState.Loading
        viewModelScope.launch {
            try {
                // Here we would ideally check network state, for simplicity if it fails we queue
                try {
                    contentRepository.sendToNotion(content)
                    _uiState.value = MainUiState.Success
                } catch (e: Exception) {
                    val errorMsg = if (e is retrofit2.HttpException) {
                        val errorString = e.response()?.errorBody()?.string() ?: ""
                        try {
                            val json = org.json.JSONObject(errorString)
                            "Notion: " + json.getString("message")
                        } catch (jsonEx: Exception) {
                            "Error ${e.code()}: $errorString"
                        }
                    } else {
                        e.message ?: "Unknown error"
                    }
                    contentRepository.saveToQueue(content)
                    _uiState.value = MainUiState.Error("Failed: $errorMsg. Saved to queue.")
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = MainUiState.Idle
    }
}
