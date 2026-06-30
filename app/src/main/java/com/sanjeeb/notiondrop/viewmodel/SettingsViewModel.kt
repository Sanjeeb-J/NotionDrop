package com.sanjeeb.notiondrop.viewmodel

import androidx.lifecycle.ViewModel
import com.sanjeeb.notiondrop.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val openApiKey: String = "",
    val geminiApiKey: String = "",
    val nvidiaApiKey: String = "",
    val aiProvider: String = "OpenAI",
    val notionToken: String = "",
    val reviewEnabled: Boolean = true,
    val databases: List<Pair<String, String>> = emptyList(),
    val appTheme: String = "System"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                openApiKey = settingsRepository.getOpenApiKey() ?: "",
                geminiApiKey = settingsRepository.getGeminiApiKey() ?: "",
                nvidiaApiKey = settingsRepository.getNvidiaApiKey() ?: "",
                aiProvider = settingsRepository.getAiProvider(),
                notionToken = settingsRepository.getNotionToken() ?: "",
                reviewEnabled = settingsRepository.getReviewEnabled(),
                databases = settingsRepository.getNotionDatabases(),
                appTheme = settingsRepository.getAppTheme()
            )
        }
    }

    fun saveOpenApiKey(key: String) {
        settingsRepository.setOpenApiKey(key)
        loadSettings()
    }

    fun saveAppTheme(theme: String) {
        settingsRepository.setAppTheme(theme)
        loadSettings()
    }

    fun saveGeminiApiKey(key: String) {
        settingsRepository.setGeminiApiKey(key)
        loadSettings()
    }

    fun saveNvidiaApiKey(key: String) {
        settingsRepository.setNvidiaApiKey(key)
        loadSettings()
    }

    fun saveAiProvider(provider: String) {
        settingsRepository.setAiProvider(provider)
        loadSettings()
    }

    fun saveNotionToken(token: String) {
        settingsRepository.setNotionToken(token)
        loadSettings()
    }

    fun setReviewEnabled(enabled: Boolean) {
        settingsRepository.setReviewEnabled(enabled)
        loadSettings()
    }

    fun addDatabase(name: String, id: String) {
        val current = settingsRepository.getNotionDatabases().toMutableList()
        current.add(name to id)
        settingsRepository.setNotionDatabases(current)
        loadSettings()
    }

    fun removeDatabase(name: String, id: String) {
        val current = settingsRepository.getNotionDatabases().toMutableList()
        current.remove(name to id)
        settingsRepository.setNotionDatabases(current)
        loadSettings()
    }
}
