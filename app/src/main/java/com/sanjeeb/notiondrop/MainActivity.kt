package com.sanjeeb.notiondrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sanjeeb.notiondrop.theme.NotionDropTheme

import dagger.hilt.android.AndroidEntryPoint

import com.sanjeeb.notiondrop.repository.SettingsRepository
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val themeState by settingsRepository.themeFlow.collectAsState()
      val isDark = when (themeState) {
          "Light" -> false
          "Dark" -> true
          else -> isSystemInDarkTheme()
      }

      NotionDropTheme(darkTheme = isDark) { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { com.sanjeeb.notiondrop.ui.navigation.NotionDropNavGraph() } }
    }
  }
}
