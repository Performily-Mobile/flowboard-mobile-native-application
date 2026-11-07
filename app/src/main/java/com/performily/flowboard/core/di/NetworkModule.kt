
package com.performily.flowboard.core.di

import com.performily.flowboard.BuildConfig
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import com.performily.flowboard.features.iam.infrastructure.local.SecureSessionStore
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

    @Provides
    @Singleton
    fun provideOkHttpClient(
        store: SecureSessionStore,
        iamRepository: Lazy<IamRepository>
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

                val response = chain.proceed(builder.build())
                // An expired or revoked token signs the user out, so the app shows the login again.
                if (response.code == 401 && !token.isNullOrBlank() &&
                    !chain.request().url.encodedPath.endsWith("/auth/login")
                ) {
                    iamRepository.get().expireSession(token)
                }
                response
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
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }
}
