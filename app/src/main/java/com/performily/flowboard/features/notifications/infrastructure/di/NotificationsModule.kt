package com.performily.flowboard.features.notifications.infrastructure.di

import com.performily.flowboard.features.notifications.domain.repository.NotificationReadStore
import com.performily.flowboard.features.notifications.domain.repository.NotificationSourceRepository
import com.performily.flowboard.features.notifications.infrastructure.local.SharedPreferencesNotificationReadStore
import com.performily.flowboard.features.notifications.infrastructure.repository.NotificationSourceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the notification sources and the local read state. */
@Module
@InstallIn(SingletonComponent::class)
interface NotificationsModule {

    @Binds
    fun bindNotificationSourceRepository(impl: NotificationSourceRepositoryImpl): NotificationSourceRepository

    @Binds
    fun bindNotificationReadStore(impl: SharedPreferencesNotificationReadStore): NotificationReadStore
}
