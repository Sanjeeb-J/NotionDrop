package com.sanjeeb.notiondrop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanjeeb.notiondrop.data.remote.StructuredContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewSheet(
    content: StructuredContent,
    onDismiss: () -> Unit,
    onConfirm: (StructuredContent) -> Unit
) {
    var editedContent by remember { mutableStateOf(content) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Review Content",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = editedContent.title,
                onValueChange = { editedContent = editedContent.copy(title = it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = editedContent.tags.joinToString(", "),
                onValueChange = { 
                    editedContent = editedContent.copy(tags = it.split(",").map { tag -> tag.trim() }.filter { tag -> tag.isNotEmpty() })
                },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Body", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = editedContent.generatedContent.ifBlank { editedContent.rawContent },
                onValueChange = { editedContent = editedContent.copy(generatedContent = it) },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                maxLines = Int.MAX_VALUE
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onConfirm(editedContent) }) {
                    Text("Send to Notion")
                }
            }
        }
    }
}
