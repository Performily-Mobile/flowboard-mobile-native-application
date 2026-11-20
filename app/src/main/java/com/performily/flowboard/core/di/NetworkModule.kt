
package com.performily.flowboard.core.di

import com.performily.flowboard.BuildConfig
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        iamRepository: Lazy<IamRepository>
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val dispatcher = Dispatcher().apply {
            maxRequests = MAX_PARALLEL_REQUESTS
            maxRequestsPerHost = MAX_PARALLEL_REQUESTS
        }

        return OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_MINUTES, TimeUnit.MINUTES))
            .addInterceptor { chain ->
                // Token of the session kept in memory.
                val token = iamRepository.get().session.value?.token
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
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

    /**
     * Requests that may run at the same time against the backend. OkHttp allows only 5 per host by
     * default, so the dashboard (one summary per area plus the other sections) waited in queues.
     */
    private const val MAX_PARALLEL_REQUESTS = 16

    /** Open connections kept for reuse, so new requests skip the TLS handshake. */
    private const val MAX_IDLE_CONNECTIONS = 8

    /** Minutes an idle connection stays open. */
    private const val KEEP_ALIVE_MINUTES = 5L
}
