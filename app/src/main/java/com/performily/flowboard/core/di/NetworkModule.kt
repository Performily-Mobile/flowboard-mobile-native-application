package com.performily.flowboard.core.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.performily.flowboard.features.iam.infrastructure.local.SecureSessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val LOCAL_URL =
        "http://127.0.0.1:8080/api/v1/"

    private const val PRODUCTION_URL =
        "https://flowboard-mobile-service.onrender.com/api/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        store: SecureSessionStore
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = store.read()?.token
                val builder = chain.request().newBuilder()

                if (!token.isNullOrBlank()) {
                    builder.header(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): Retrofit {

        val isDebug =
            (context.applicationInfo.flags and
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val baseUrl = if (isDebug) {
            LOCAL_URL
        } else {
            PRODUCTION_URL
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }
}

