package com.sanjeeb.notiondrop.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanjeeb.notiondrop.R
import com.sanjeeb.notiondrop.viewmodel.MainUiState
import com.sanjeeb.notiondrop.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isAiExpanded by remember { mutableStateOf(true) }

    val databases by viewModel.databases.collectAsState()
    val selectedDatabaseId by viewModel.selectedDatabaseId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDatabases()
        val activity = context as? android.app.Activity
        val intent = activity?.intent
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                inputText = sharedText
            }
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(speechRecognizer, context) { result ->
                inputText += " $result"
                isListening = false
            }
            isListening = true
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = false,
                    onClick = onNavigateToHistory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = onNavigateToSettings
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isListening) {
                        speechRecognizer.stopListening()
                        isListening = false
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                containerColor = if (isListening) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                contentColor = if (isListening) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.notion_airdrop_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(42.dp)
                                .padding(end = 12.dp)
                        )
                        Text(
                            "NotionDrop",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        try {
                            if (clipboard.hasPrimaryClip()) {
                                val item: ClipData.Item? = clipboard.primaryClip?.getItemAt(0)
                                val pasteData = item?.text?.toString()
                                if (pasteData != null) {
                                    inputText += pasteData
                                }
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(context, "Clipboard access denied", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("What's on your mind?") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                )
                
                Text(
                    text = "${inputText.length} chars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = inputText.isNotBlank(),
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    Column {
                        if (databases.size > 1) {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedName = databases.find { it.second == selectedDatabaseId }?.first ?: "Auto-Select"

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Target Database") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Auto-Select (Let AI Decide)") },
                                        onClick = {
                                            viewModel.selectDatabase(null)
                                            expanded = false
                                        }
                                    )
                                    databases.forEach { db ->
                                        DropdownMenuItem(
                                            text = { Text(db.first) },
                                            onClick = {
                                                viewModel.selectDatabase(db.second)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AI Expand", style = MaterialTheme.typography.titleMedium)
                                Text("Let AI generate detailed content", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isAiExpanded,
                                onCheckedChange = { isAiExpanded = it }
                            )
                        }
                        Button(
                            onClick = { viewModel.processContent(inputText, isAiExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = uiState !is MainUiState.Loading
                        ) {
                        if (uiState is MainUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send to Notion", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

    if (uiState is MainUiState.Review) {
        PreviewSheet(
            content = (uiState as MainUiState.Review).content,
            onDismiss = { viewModel.resetState() },
            onConfirm = { viewModel.sendToNotion(it) }
        )
    }
    
    if (uiState is MainUiState.Success) {
        val composition by com.airbnb.lottie.compose.rememberLottieComposition(
            com.airbnb.lottie.compose.LottieCompositionSpec.Url("https://lottie.host/80c4ef1e-ebad-4876-8805-728b7e7161dc/Yf1T9sD0yX.json") // Simple success animation URL
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            com.airbnb.lottie.compose.LottieAnimation(
                composition = composition,
                modifier = Modifier.size(200.dp),
                iterations = 1
            )
        }
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Successfully sent to Notion!", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(2000)
            inputText = ""
            viewModel.resetState()
        }
    } else if (uiState is MainUiState.Error) {
        LaunchedEffect(uiState) {
            Toast.makeText(context, (uiState as MainUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }
}

private fun startListening(
    recognizer: SpeechRecognizer,
    context: Context,
    onResult: (String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            Toast.makeText(context, "Speech recognition error: $error", Toast.LENGTH_SHORT).show()
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    recognizer.startListening(intent)
}
