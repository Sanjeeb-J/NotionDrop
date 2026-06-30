package com.sanjeeb.notiondrop.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Instructions") },
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
                Text(
                    text = "How to setup APIs & Integrations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                InstructionSection(
                    title = "1. OpenAI API Key",
                    description = "To use OpenAI as your AI provider, you need an API key. Click the link below, log in to your OpenAI account, and create a new secret key.",
                    linkUrl = "https://platform.openai.com/api-keys",
                    linkText = "OpenAI API Keys Page",
                    context = context
                )
            }

            item {
                InstructionSection(
                    title = "2. NVIDIA API Key",
                    description = "You can also use NVIDIA as your AI provider! Generate an API key from NVIDIA Build and simply paste it into the 'OpenAI API Key' field in the settings.",
                    linkUrl = "https://build.nvidia.com/settings/api-keys",
                    linkText = "NVIDIA API Keys Page",
                    context = context
                )
            }

            item {
                InstructionSection(
                    title = "3. Google AI Studio (Gemini) API Key",
                    description = "To use Gemini as your AI provider, obtain an API key from Google AI Studio. Click the link below, sign in with your Google account, and create a new API key.",
                    linkUrl = "https://aistudio.google.com/app/api-keys",
                    linkText = "Google AI Studio API Keys Page",
                    context = context
                )
            }

            item {
                InstructionSection(
                    title = "4. Notion Integration Token",
                    description = "To allow the app to write to your Notion databases, you must create an integration token. Go to Notion's My Connections page, click 'Develop or manage integrations', and create a new internal integration. Copy the 'Internal Integration Secret' and paste it in Settings.",
                    linkUrl = "https://app.notion.com/developers/connections",
                    linkText = "Notion Connections Page",
                    context = context
                )
            }

            item {
                InstructionSection(
                    title = "5. Adding a Notion Database ID",
                    description = "In order to save items to a database, you need its Database ID:\n" +
                            "• Open the desired Notion database as a full page in your browser.\n" +
                            "• Look at the URL: https://www.notion.so/workspace/DATABASE_ID?v=...\n" +
                            "• The string of 32 characters before the '?v=' is your Database ID.\n" +
                            "• IMPORTANT: You must also invite your newly created Integration to this database. Go to the Database in Notion, click the three dots (···) at the top right, select 'Add connections', and search for the name of the integration you created in step 3.",
                    linkUrl = null,
                    linkText = null,
                    context = context
                )
            }

            item {
                InstructionSection(
                    title = "6. Database Design (Required)",
                    description = "Your Notion database must be designed with the following specific properties for the app to work correctly:\n" +
                            "• 'Name' (Title property, denoted by 'Aa')\n" +
                            "• 'Tags' (Multi-select property, denoted by the list icon)\n" +
                            "• 'Date' (Date property, denoted by the calendar icon)\n\n" +
                            "Make sure the property names match exactly (case-sensitive).",
                    linkUrl = null,
                    linkText = null,
                    context = context
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InstructionSection(
    title: String,
    description: String,
    linkUrl: String?,
    linkText: String?,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
        )
        if (linkUrl != null && linkText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(linkText)
            }
        }
    }
}
