package com.sanjeeb.notiondrop.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.sanjeeb.notiondrop.data.local.AppDatabase
import com.sanjeeb.notiondrop.data.local.HistoryDao
import com.sanjeeb.notiondrop.data.remote.NotionApi
import com.sanjeeb.notiondrop.data.remote.OpenAIApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notiondrop_database"
        ).build()
    }

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApi(okHttpClient: OkHttpClient, gson: Gson): OpenAIApi {
        return Retrofit.Builder()
            .baseUrl("https://integrate.api.nvidia.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotionApi(okHttpClient: OkHttpClient, gson: Gson): NotionApi {
        return Retrofit.Builder()
            .baseUrl("https://api.notion.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NotionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApi(okHttpClient: OkHttpClient, gson: Gson): com.sanjeeb.notiondrop.data.remote.GeminiApi {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(com.sanjeeb.notiondrop.data.remote.GeminiApi::class.java)
    }
}
