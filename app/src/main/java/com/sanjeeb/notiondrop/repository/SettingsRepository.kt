package com.sanjeeb.notiondrop.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "notiondrop_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _themeFlow = kotlinx.coroutines.flow.MutableStateFlow(getAppTheme())
    val themeFlow: kotlinx.coroutines.flow.StateFlow<String> = _themeFlow.asStateFlow()

    fun getAppTheme(): String = sharedPreferences.getString("app_theme", "System") ?: "System"
    fun setAppTheme(theme: String) {
        sharedPreferences.edit().putString("app_theme", theme).apply()
        _themeFlow.value = theme
    }

    fun getOpenApiKey(): String? = sharedPreferences.getString("openai_key", null)
    fun setOpenApiKey(key: String) = sharedPreferences.edit().putString("openai_key", key).apply()

    fun getGeminiApiKey(): String? = sharedPreferences.getString("gemini_key", null)
    fun setGeminiApiKey(key: String) = sharedPreferences.edit().putString("gemini_key", key).apply()

    fun getNvidiaApiKey(): String? = sharedPreferences.getString("nvidia_key", null)
    fun setNvidiaApiKey(key: String) = sharedPreferences.edit().putString("nvidia_key", key).apply()

    fun getAiProvider(): String = sharedPreferences.getString("ai_provider", "OpenAI") ?: "OpenAI"
    fun setAiProvider(provider: String) = sharedPreferences.edit().putString("ai_provider", provider).apply()

    fun getNotionToken(): String? = sharedPreferences.getString("notion_token", null)
    fun setNotionToken(token: String) = sharedPreferences.edit().putString("notion_token", token).apply()

    fun getReviewEnabled(): Boolean = sharedPreferences.getBoolean("review_enabled", true)
    fun setReviewEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("review_enabled", enabled).apply()

    // Comma separated list of "Name|ID"
    fun getNotionDatabases(): List<Pair<String, String>> {
        val raw = sharedPreferences.getString("notion_databases", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun setNotionDatabases(databases: List<Pair<String, String>>) {
        val raw = databases.joinToString(",") { "${it.first}|${it.second}" }
        sharedPreferences.edit().putString("notion_databases", raw).apply()
    }
}
