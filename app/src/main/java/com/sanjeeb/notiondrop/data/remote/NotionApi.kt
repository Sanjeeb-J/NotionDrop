package com.sanjeeb.notiondrop.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NotionApi {
    @POST("pages")
    suspend fun createPage(
        @Header("Authorization") authHeader: String,
        @Header("Notion-Version") notionVersion: String = "2022-06-28",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: NotionPageRequest
    ): NotionPageResponse
}

data class NotionPageRequest(
    val parent: NotionParent,
    val properties: Map<String, NotionProperty>,
    val children: List<NotionBlock>
)

data class NotionParent(
    val database_id: String
)

sealed class NotionProperty {
    data class Title(val title: List<NotionText>) : NotionProperty()
    data class MultiSelect(val multi_select: List<NotionSelectOption>) : NotionProperty()
    data class Select(val select: NotionSelectOption) : NotionProperty()
    data class Date(val date: NotionDateValue) : NotionProperty()
}

data class NotionText(
    val text: NotionTextContent,
    val annotations: NotionAnnotations? = null
)

data class NotionAnnotations(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val strikethrough: Boolean = false,
    val underline: Boolean = false,
    val code: Boolean = false,
    val color: String = "default"
)

data class NotionTextContent(
    val content: String
)

data class NotionSelectOption(
    val name: String
)

data class NotionDateValue(
    val start: String // ISO 8601 date string
)

data class NotionBlock(
    val `object`: String = "block",
    val type: String,
    val paragraph: NotionBlockContent? = null,
    val heading_1: NotionBlockContent? = null,
    val heading_2: NotionBlockContent? = null,
    val heading_3: NotionBlockContent? = null,
    val bulleted_list_item: NotionBlockContent? = null,
    val numbered_list_item: NotionBlockContent? = null,
    val to_do: NotionTodoContent? = null,
    val toggle: NotionBlockContent? = null,
    val callout: NotionCalloutContent? = null,
    val code: NotionCodeContent? = null,
    val divider: NotionDividerContent? = null
)

class NotionDividerContent

data class NotionBlockContent(
    val rich_text: List<NotionText>
)

data class NotionCodeContent(
    val rich_text: List<NotionText>,
    val language: String = "plain text"
)

data class NotionTodoContent(
    val rich_text: List<NotionText>,
    val checked: Boolean
)

data class NotionCalloutContent(
    val rich_text: List<NotionText>,
    val icon: NotionIcon? = NotionIcon(emoji = "💡")
)

data class NotionIcon(
    val type: String = "emoji",
    val emoji: String
)

data class NotionPageResponse(
    val id: String,
    val url: String
)
