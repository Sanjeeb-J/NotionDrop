package com.sanjeeb.notiondrop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanjeeb.notiondrop.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var openApiKey by remember(uiState.openApiKey) { mutableStateOf(uiState.openApiKey) }
    var geminiApiKey by remember(uiState.geminiApiKey) { mutableStateOf(uiState.geminiApiKey) }
    var aiProvider by remember(uiState.aiProvider) { mutableStateOf(uiState.aiProvider) }
    var notionToken by remember(uiState.notionToken) { mutableStateOf(uiState.notionToken) }
    
    var newDbName by remember { mutableStateOf("") }
    var newDbId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Appearance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            
            item {
                var expanded by remember { mutableStateOf(false) }
                val themes = listOf("System", "Light", "Dark")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.appTheme,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("App Theme") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        themes.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme) },
                                onClick = {
                                    viewModel.saveAppTheme(theme)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("API Credentials", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Provider", style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = aiProvider == "OpenAI",
                            onClick = {
                                aiProvider = "OpenAI"
                                viewModel.saveAiProvider("OpenAI")
                            },
                            label = { Text("OpenAI") }
                        )
                        FilterChip(
                            selected = aiProvider == "Gemini",
                            onClick = {
                                aiProvider = "Gemini"
                                viewModel.saveAiProvider("Gemini")
                            },
                            label = { Text("Gemini") }
                        )
                    }
                }
            }
            
            item {
                if (aiProvider == "OpenAI") {
                    OutlinedTextField(
                        value = openApiKey,
                        onValueChange = { 
                            openApiKey = it
                            viewModel.saveOpenApiKey(it) 
                        },
                        label = { Text("OpenAI API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (aiProvider == "Gemini") {
                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { 
                            geminiApiKey = it
                            viewModel.saveGeminiApiKey(it) 
                        },
                        label = { Text("Google AI Studio (Gemini) API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                OutlinedTextField(
                    value = notionToken,
                    onValueChange = { 
                        notionToken = it
                        viewModel.saveNotionToken(it) 
                    },
                    label = { Text("Notion Integration Token") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Review before sending", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.reviewEnabled,
                        onCheckedChange = { viewModel.setReviewEnabled(it) }
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Notion Databases", style = MaterialTheme.typography.titleMedium)
            }
            
            items(uiState.databases) { db ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(db.first, style = MaterialTheme.typography.bodyLarge)
                        Text(db.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.removeDatabase(db.first, db.second) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = newDbName,
                            onValueChange = { newDbName = it },
                            label = { Text("Name (e.g. Notes)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newDbId,
                            onValueChange = { newDbId = it },
                            label = { Text("Database ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    IconButton(
                        onClick = {
                            if (newDbName.isNotBlank() && newDbId.isNotBlank()) {
                                viewModel.addDatabase(newDbName, newDbId)
                                newDbName = ""
                                newDbId = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToHelp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Help & Instructions")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
