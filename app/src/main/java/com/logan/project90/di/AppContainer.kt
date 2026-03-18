package com.logan.project90.di

import android.content.Context
import androidx.room.Room
import com.logan.project90.data.local.AppDatabase
import com.logan.project90.data.local.datastore.SettingsDataStore
import com.logan.project90.data.repository.DailyLogRepositoryImpl
import com.logan.project90.data.repository.ExperimentRepositoryImpl
import com.logan.project90.data.repository.IdentityRepositoryImpl
import com.logan.project90.domain.repository.DailyLogRepository
import com.logan.project90.domain.repository.ExperimentRepository
import com.logan.project90.domain.repository.IdentityRepository
import com.logan.project90.domain.repository.SettingsRepository
import com.logan.project90.domain.usecase.CalculateIdentityAnalyticsUseCase
import com.logan.project90.domain.usecase.CompleteOnboardingUseCase
import com.logan.project90.domain.usecase.CreateExperimentUseCase
import com.logan.project90.domain.usecase.CreateIdentityUseCase
import com.logan.project90.domain.usecase.DeleteIdentityUseCase
import com.logan.project90.domain.usecase.GenerateFeedbackUseCase
import com.logan.project90.domain.usecase.GetEditableIdentityUseCase
import com.logan.project90.domain.usecase.GetIdentityDetailUseCase
import com.logan.project90.domain.usecase.GetTodaySliceUseCase
import com.logan.project90.domain.usecase.LogIdentityDayUseCase
import com.logan.project90.domain.usecase.UpdateIdentityUseCase

interface AppContainer {
    val settingsRepository: SettingsRepository
    val experimentRepository: ExperimentRepository
    val identityRepository: IdentityRepository
    val dailyLogRepository: DailyLogRepository
    val completeOnboardingUseCase: CompleteOnboardingUseCase
    val createExperimentUseCase: CreateExperimentUseCase
    val createIdentityUseCase: CreateIdentityUseCase
    val updateIdentityUseCase: UpdateIdentityUseCase
    val deleteIdentityUseCase: DeleteIdentityUseCase
    val getEditableIdentityUseCase: GetEditableIdentityUseCase
    val logIdentityDayUseCase: LogIdentityDayUseCase
    val calculateIdentityAnalyticsUseCase: CalculateIdentityAnalyticsUseCase
    val generateFeedbackUseCase: GenerateFeedbackUseCase
    val getTodaySliceUseCase: GetTodaySliceUseCase
    val getIdentityDetailUseCase: GetIdentityDetailUseCase
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "project90.db"
    ).build()

    override val settingsRepository: SettingsRepository = SettingsDataStore(context)
    override val experimentRepository: ExperimentRepository = ExperimentRepositoryImpl(database.experimentDao())
    override val identityRepository: IdentityRepository = IdentityRepositoryImpl(
        dao = database.identityDao(),
        dailyLogDao = database.dailyLogDao(),
        database = database
    )
    override val dailyLogRepository: DailyLogRepository = DailyLogRepositoryImpl(database.dailyLogDao())

    override val completeOnboardingUseCase = CompleteOnboardingUseCase(settingsRepository)
    override val createExperimentUseCase = CreateExperimentUseCase(experimentRepository)
    override val createIdentityUseCase = CreateIdentityUseCase(identityRepository, settingsRepository)
    override val updateIdentityUseCase = UpdateIdentityUseCase(identityRepository, settingsRepository)
    override val deleteIdentityUseCase = DeleteIdentityUseCase(identityRepository)
    override val getEditableIdentityUseCase = GetEditableIdentityUseCase(identityRepository, experimentRepository)
    override val logIdentityDayUseCase = LogIdentityDayUseCase(dailyLogRepository)
    override val calculateIdentityAnalyticsUseCase = CalculateIdentityAnalyticsUseCase(dailyLogRepository)
    override val generateFeedbackUseCase = GenerateFeedbackUseCase(
        identityRepository = identityRepository,
        settingsRepository = settingsRepository
    )
    override val getTodaySliceUseCase = GetTodaySliceUseCase(
        experimentRepository = experimentRepository,
        identityRepository = identityRepository,
        dailyLogRepository = dailyLogRepository,
        analyticsUseCase = calculateIdentityAnalyticsUseCase,
        feedbackUseCase = generateFeedbackUseCase
    )
    override val getIdentityDetailUseCase = GetIdentityDetailUseCase(
        identityRepository = identityRepository,
        dailyLogRepository = dailyLogRepository,
        analyticsUseCase = calculateIdentityAnalyticsUseCase,
        feedbackUseCase = generateFeedbackUseCase
    )
}
