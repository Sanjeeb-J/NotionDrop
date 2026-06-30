package com.sanjeeb.notiondrop.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("chat/completions")
    suspend fun getStructuredContent(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

data class OpenAIRequest(
    val model: String = "meta/llama-3.1-8b-instruct",
    val messages: List<Message>,
    @SerializedName("response_format") val responseFormat: ResponseFormat = ResponseFormat(type = "json_object")
)

data class Message(
    val role: String,
    val content: String
)

data class ResponseFormat(
    val type: String
)

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class StructuredContent(
    val title: String,
    val tags: List<String>,
    @SerializedName("target_database") var targetDatabase: String,
    @SerializedName("generated_content") val generatedContent: String = "",
    @Transient var rawContent: String = ""
)
