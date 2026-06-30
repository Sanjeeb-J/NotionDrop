package com.sanjeeb.notiondrop.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

import retrofit2.http.Url

interface GeminiApi {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
