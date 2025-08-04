package com.dls.pymetask.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import com.dls.pymetask.utils.AlarmUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmUtils {
        return AlarmUtils(context)
    }
}