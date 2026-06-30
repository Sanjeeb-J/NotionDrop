package com.sanjeeb.notiondrop.repository

import com.google.gson.Gson
import com.sanjeeb.notiondrop.data.local.HistoryDao
import com.sanjeeb.notiondrop.data.local.HistoryEntry
import com.sanjeeb.notiondrop.data.remote.Message
import com.sanjeeb.notiondrop.data.remote.NotionApi
import com.sanjeeb.notiondrop.data.remote.NotionAnnotations
import com.sanjeeb.notiondrop.data.remote.NotionBlock
import com.sanjeeb.notiondrop.data.remote.NotionBlockContent
import com.sanjeeb.notiondrop.data.remote.NotionCalloutContent
import com.sanjeeb.notiondrop.data.remote.NotionCodeContent
import com.sanjeeb.notiondrop.data.remote.NotionDividerContent
import com.sanjeeb.notiondrop.data.remote.NotionDateValue
import com.sanjeeb.notiondrop.data.remote.NotionPageRequest
import com.sanjeeb.notiondrop.data.remote.NotionParent
import com.sanjeeb.notiondrop.data.remote.NotionProperty
import com.sanjeeb.notiondrop.data.remote.NotionSelectOption
import com.sanjeeb.notiondrop.data.remote.NotionText
import com.sanjeeb.notiondrop.data.remote.NotionTextContent
import com.sanjeeb.notiondrop.data.remote.NotionTodoContent
import com.sanjeeb.notiondrop.data.remote.OpenAIApi
import com.sanjeeb.notiondrop.data.remote.OpenAIRequest
import com.sanjeeb.notiondrop.data.remote.GeminiApi
import com.sanjeeb.notiondrop.data.remote.GeminiRequest
import com.sanjeeb.notiondrop.data.remote.GeminiContent
import com.sanjeeb.notiondrop.data.remote.GeminiPart
import com.sanjeeb.notiondrop.data.remote.GeminiGenerationConfig
import com.sanjeeb.notiondrop.data.remote.StructuredContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val geminiApi: GeminiApi,
    private val notionApi: NotionApi,
    private val historyDao: HistoryDao,
    private val settingsRepository: SettingsRepository,
    private val gson: Gson
) {
    suspend fun processContent(rawContent: String, isAiExpanded: Boolean = true, targetDatabaseId: String? = null): StructuredContent {
        val provider = settingsRepository.getAiProvider()
        val databases = settingsRepository.getNotionDatabases()
        if (databases.isEmpty()) throw Exception("No Notion databases configured")

        val databaseNames = databases.joinToString(", ") { it.first }

        val systemPrompt = if (isAiExpanded) {
            """
                You are a helpful AI assistant. The user will give you a prompt or raw text.
                Your job is to generate a comprehensive response or expand on their notes.
                
                1. Extract a short title (max 8 words)
                2. Extract up to 4 relevant tags
                3. Generate the actual detailed content/response for the user's prompt
                4. Pick the most suitable target_database from the list provided: [$databaseNames]

                CRITICAL INSTRUCTION FOR GENERATED CONTENT:
                Format your generated content beautifully using Markdown, matching a high-quality structured note style.
                - Use relevant emojis for headings (e.g., `## 🥇 1. Option Name ⭐⭐⭐⭐⭐`).
                - Use bold text for emphasis or important labels (e.g., `**Pick this one.**`).
                - Use bullet points (`* ` or `- `) and clean spacing to separate ideas or list pros/cons.
                - Use code blocks for commands, models, or technical terms.
                - Keep it highly structured, readable, and visually appealing.
                - Use `# `, `## `, `### ` for headings, `> ` for callouts, and `[ ] ` for checklists if needed.
                
                Return ONLY a valid JSON object in this exact schema:
                {
                  "title": "string",
                  "tags": ["string"],
                  "target_database": "database_name_from_user_list",
                  "generated_content": "string"
                }

                Do not include any explanation or markdown outside the JSON. Return raw JSON only.
            """.trimIndent()
        } else {
            """
                You are a content structuring assistant. The user will give you raw unstructured content.
                Your job is to strictly extract metadata without generating or expanding any content.
                
                1. Extract a short title (max 8 words)
                2. Extract up to 4 relevant tags
                3. Pick the most suitable target_database from the list provided: [$databaseNames]
                4. Set generated_content to an empty string ("")

                Return ONLY a valid JSON object in this exact schema:
                {
                  "title": "string",
                  "tags": ["string"],
                  "target_database": "database_name_from_user_list",
                  "generated_content": ""
                }

                Do not include any explanation or markdown. Return raw JSON only.
            """.trimIndent()
        }

        val jsonContent = if (provider == "Gemini" || provider == "Google AI Studio") {
            val geminiKey = settingsRepository.getGeminiApiKey() ?: throw Exception("Gemini API Key not set")
            val request = GeminiRequest(
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)), role = "user"),
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = rawContent)), role = "user")
                ),
                generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
            )
            val response = geminiApi.generateContent(geminiKey.trim(), request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Empty response from Gemini")
        } else {
            val openApiKey = settingsRepository.getOpenApiKey() ?: throw Exception("OpenAI API Key not set")
            val request = OpenAIRequest(
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = rawContent)
                )
            )
            val response = openAIApi.getStructuredContent("Bearer ${openApiKey.trim()}", request)
            response.choices.firstOrNull()?.message?.content ?: throw Exception("Empty response from AI")
        }
        
        val cleanJson = jsonContent.replace(Regex("^```json\\s*"), "").replace(Regex("\\s*```$"), "").trim()
        val parsed = gson.fromJson(cleanJson, StructuredContent::class.java)
        parsed.rawContent = rawContent
        if (targetDatabaseId != null) {
            parsed.targetDatabase = databases.find { it.second == targetDatabaseId }?.first ?: targetDatabaseId
        }
        return parsed
    }

    suspend fun sendToNotion(structuredContent: StructuredContent) {
        val notionToken = settingsRepository.getNotionToken() ?: throw Exception("Notion Token not set")
        val databases = settingsRepository.getNotionDatabases()
        
        // Find matching database ID
        val targetDbName = structuredContent.targetDatabase
        val dbId = databases.find { it.first.equals(targetDbName, ignoreCase = true) }?.second
            ?: databases.firstOrNull()?.second // Fallback to first if not matched
            ?: throw Exception("Target database not found")

        // If the AI generated an expanded response, use it. Otherwise fallback to raw input.
        val contentToSave = if (structuredContent.generatedContent.isNotBlank()) {
            structuredContent.generatedContent
        } else {
            structuredContent.rawContent
        }

        // Parse markdown content into NotionBlocks
        val notionBlocks = parseMarkdownToNotionBlocks(contentToSave)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date())

        val properties = mapOf(
            "Name" to NotionProperty.Title(listOf(NotionText(NotionTextContent(structuredContent.title)))),
            "Tags" to NotionProperty.MultiSelect(structuredContent.tags.map { NotionSelectOption(it) }),
            "Date" to NotionProperty.Date(NotionDateValue(today))
        )

        val request = NotionPageRequest(
            parent = NotionParent(database_id = dbId),
            properties = properties,
            children = notionBlocks
        )

        notionApi.createPage("Bearer ${notionToken.trim()}", request = request)

        // Save to history on success
        historyDao.insertEntry(
            HistoryEntry(
                title = structuredContent.title,
                type = "note",
                content = gson.toJson(structuredContent),
                targetDatabaseName = targetDbName,
                timestamp = System.currentTimeMillis(),
                status = "SENT"
            )
        )
    }

    suspend fun saveToQueue(structuredContent: StructuredContent) {
        historyDao.insertEntry(
            HistoryEntry(
                title = structuredContent.title,
                type = "note",
                content = gson.toJson(structuredContent),
                targetDatabaseName = structuredContent.targetDatabase,
                timestamp = System.currentTimeMillis(),
                status = "QUEUED"
            )
        )
    }

    private fun parseMarkdownToNotionBlocks(markdown: String): List<NotionBlock> {
        val blocks = mutableListOf<NotionBlock>()
        val lines = markdown.split("\n")
        
        var inCodeBlock = false
        var currentCode = StringBuilder()
        var codeLanguage = "plain text"

        for (line in lines) {
            val trimmed = line.trim()
            
            if (inCodeBlock) {
                if (trimmed.startsWith("```")) {
                    blocks.add(NotionBlock(type = "code", code = NotionCodeContent(listOf(NotionText(NotionTextContent(currentCode.toString().trimEnd()))), language = codeLanguage)))
                    inCodeBlock = false
                    currentCode.clear()
                } else {
                    currentCode.append(line).append("\n")
                }
                continue
            }

            if (trimmed.startsWith("```")) {
                inCodeBlock = true
                val lang = trimmed.removePrefix("```").trim()
                codeLanguage = if (lang.isNotEmpty()) lang else "plain text"
                continue
            }

            if (trimmed.isEmpty()) continue
            
            when {
                trimmed == "---" -> {
                    blocks.add(NotionBlock(type = "divider", divider = NotionDividerContent()))
                }
                trimmed.startsWith("# ") -> {
                    blocks.add(NotionBlock(type = "heading_1", heading_1 = NotionBlockContent(parseInlineMarkdown(trimmed.substring(2).trim()))))
                }
                trimmed.startsWith("## ") -> {
                    blocks.add(NotionBlock(type = "heading_2", heading_2 = NotionBlockContent(parseInlineMarkdown(trimmed.substring(3).trim()))))
                }
                trimmed.startsWith("### ") -> {
                    blocks.add(NotionBlock(type = "heading_3", heading_3 = NotionBlockContent(parseInlineMarkdown(trimmed.substring(4).trim()))))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    blocks.add(NotionBlock(type = "bulleted_list_item", bulleted_list_item = NotionBlockContent(parseInlineMarkdown(trimmed.substring(2).trim()))))
                }
                trimmed.matches(Regex("^[0-9]+\\.\\s.*")) -> {
                    val content = trimmed.substringAfter(". ").trim()
                    blocks.add(NotionBlock(type = "numbered_list_item", numbered_list_item = NotionBlockContent(parseInlineMarkdown(content))))
                }
                trimmed.startsWith("[ ] ") -> {
                    blocks.add(NotionBlock(type = "to_do", to_do = NotionTodoContent(parseInlineMarkdown(trimmed.substring(4).trim()), checked = false)))
                }
                trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") -> {
                    blocks.add(NotionBlock(type = "to_do", to_do = NotionTodoContent(parseInlineMarkdown(trimmed.substring(4).trim()), checked = true)))
                }
                trimmed.startsWith(">> ") -> {
                    blocks.add(NotionBlock(type = "toggle", toggle = NotionBlockContent(parseInlineMarkdown(trimmed.substring(3).trim()))))
                }
                trimmed.startsWith("> ") -> {
                    blocks.add(NotionBlock(type = "callout", callout = NotionCalloutContent(parseInlineMarkdown(trimmed.substring(2).trim()))))
                }
                else -> {
                    // Split long paragraphs if they exceed 2000 chars
                    val chunks = trimmed.chunked(2000)
                    for (chunk in chunks) {
                        blocks.add(NotionBlock(type = "paragraph", paragraph = NotionBlockContent(parseInlineMarkdown(chunk))))
                    }
                }
            }
        }
        
        if (inCodeBlock && currentCode.isNotEmpty()) {
            blocks.add(NotionBlock(type = "code", code = NotionCodeContent(listOf(NotionText(NotionTextContent(currentCode.toString().trimEnd()))), language = codeLanguage)))
        }

        return if (blocks.isEmpty()) {
            listOf(NotionBlock(type = "paragraph", paragraph = NotionBlockContent(parseInlineMarkdown(markdown))))
        } else {
            blocks
        }
    }

    private fun parseInlineMarkdown(text: String): List<NotionText> {
        val result = mutableListOf<NotionText>()
        val pattern = Regex("\\*\\*(.*?)\\*\\*|\\*(.*?)\\*|`(.*?)`")
        var lastIndex = 0
        
        pattern.findAll(text).forEach { matchResult ->
            if (matchResult.range.first > lastIndex) {
                result.add(NotionText(text = NotionTextContent(text.substring(lastIndex, matchResult.range.first))))
            }
            
            if (matchResult.groups[1] != null) {
                result.add(NotionText(text = NotionTextContent(matchResult.groups[1]!!.value), annotations = NotionAnnotations(bold = true)))
            } else if (matchResult.groups[2] != null) {
                result.add(NotionText(text = NotionTextContent(matchResult.groups[2]!!.value), annotations = NotionAnnotations(italic = true)))
            } else if (matchResult.groups[3] != null) {
                result.add(NotionText(text = NotionTextContent(matchResult.groups[3]!!.value), annotations = NotionAnnotations(code = true)))
            }
            
            lastIndex = matchResult.range.last + 1
        }
        
        if (lastIndex < text.length) {
            result.add(NotionText(text = NotionTextContent(text.substring(lastIndex))))
        }
        
        if (result.isEmpty()) {
            result.add(NotionText(text = NotionTextContent(text)))
        }
        
        return result
    }

}
