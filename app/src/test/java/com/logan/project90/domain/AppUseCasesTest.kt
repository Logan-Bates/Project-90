package com.logan.project90.domain

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.FeedbackType
import com.logan.project90.domain.model.Identity
import com.logan.project90.domain.model.IdentityAnalytics
import com.logan.project90.domain.model.TodayIdentityCardModel
import com.logan.project90.domain.repository.DailyLogRepository
import com.logan.project90.domain.repository.IdentityRepository
import com.logan.project90.domain.repository.SettingsRepository
import com.logan.project90.domain.usecase.CalculateIdentityAnalyticsUseCase
import com.logan.project90.domain.usecase.GenerateFeedbackUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AppUseCasesTest {
    private val useCase = CalculateIdentityAnalyticsUseCase(FakeDailyLogRepository())

    @Test
    fun dailyIdentityPoints_capsPushPlusHighResistance() {
        val log = DailyLog(
            identityId = 1,
            logDate = LocalDate.of(2026, 3, 16),
            effortMinutes = 60,
            status = IdentityStatus.PUSH_EXECUTED,
            energy = 3,
            mood = 3,
            resistance = ResistanceLevel.HIGH,
            reflection = ""
        )

        assertEquals(1.75, useCase.dailyIdentityPoints(log), 0.0001)
    }

    @Test
    fun recoveryBalance_fallsWhenPushRatioExceedsThreshold() {
        val recovery = useCase.calculateRecoveryBalance14(
            pushDays14 = 10,
            activeDays14 = 14,
            avgEnergy7 = 4.0,
            resistanceTrend5 = 0.0
        )

        assertTrue(recovery < 100.0)
    }

    @Test
    fun momentum_usesAdaptiveBurnoutWeighting() {
        val normal = useCase.calculateMomentum(80.0, 60.0, 50.0, burnoutTrigger = false)
        val burnout = useCase.calculateMomentum(80.0, 60.0, 50.0, burnoutTrigger = true)

        assertEquals(67.0, normal, 0.0001)
        assertEquals(64.0, burnout, 0.0001)
    }

    @Test
    fun feedback_prioritizesBurnoutAndSuppressesPushGuidance() {
        val feedback = runExperimentFeedback(
            analytics = analytics(
                strength14 = 78.0,
                strength7 = 72.0,
                pushFreq14 = 12.0,
                recoveryBalance14 = 65.0,
                resistanceTrend5 = 0.3,
                avgEnergy7 = 2.0,
                burnoutTrigger = true
            ),
            totalFloorMinutes = 120,
            discretionaryTimeMinutes = 160
        )

        assertEquals(FeedbackType.BURNOUT_RISK, feedback?.type)
    }

    @Test
    fun feedback_usesNeutralPushGuidanceWhenFloorIsStrong() {
        val feedback = runIdentityFeedback(
            analytics = analytics(
                strength14 = 82.0,
                strength7 = 80.0,
                pushFreq14 = 10.0,
                recoveryBalance14 = 92.0
            ),
            totalFloorMinutes = 45,
            discretionaryTimeMinutes = 180
        )

        assertEquals(FeedbackType.PUSH_GUIDANCE, feedback?.type)
        assertEquals(
            "Floor consistency is strong. Increase push only if it feels sustainable.",
            feedback?.message
        )
    }

    @Test
    fun feedback_returnsPositiveStateWhenNoWarningsApply() {
        val feedback = runIdentityFeedback(
            analytics = analytics(
                strength14 = 84.0,
                strength7 = 78.0,
                pushFreq14 = 28.0,
                recoveryBalance14 = 88.0
            ),
            totalFloorMinutes = 45,
            discretionaryTimeMinutes = 180
        )

        assertEquals(FeedbackType.POSITIVE_STEADY_STATE, feedback?.type)
    }

    private fun runExperimentFeedback(
        analytics: IdentityAnalytics,
        totalFloorMinutes: Int,
        discretionaryTimeMinutes: Int
    ) = kotlinx.coroutines.runBlocking {
        val useCase = GenerateFeedbackUseCase(
            identityRepository = FakeIdentityRepository(totalFloorMinutes),
            settingsRepository = FakeSettingsRepository(discretionaryTimeMinutes)
        )
        useCase.experimentFeedback(
            experiment = Experiment(
                id = 1,
                name = "Project 90",
                startDate = LocalDate.of(2026, 3, 1),
                durationDays = 90,
                endDate = LocalDate.of(2026, 5, 29)
            ),
            identityCards = listOf(
                TodayIdentityCardModel(
                    identity = Identity(
                        id = 1,
                        experimentId = 1,
                        name = "Deep Work",
                        statement = "I protect focused time every day.",
                        category = IdentityCategory.MIND,
                        floorMinutes = 45,
                        pushMinutes = 90,
                        importanceWeight = 2,
                        createdDate = LocalDate.of(2026, 3, 1)
                    ),
                    todayLog = null,
                    analytics = analytics,
                    feedback = null
                )
            )
        )
    }

    private fun runIdentityFeedback(
        analytics: IdentityAnalytics,
        totalFloorMinutes: Int,
        discretionaryTimeMinutes: Int
    ) = kotlinx.coroutines.runBlocking {
        val useCase = GenerateFeedbackUseCase(
            identityRepository = FakeIdentityRepository(totalFloorMinutes),
            settingsRepository = FakeSettingsRepository(discretionaryTimeMinutes)
        )
        useCase.identityFeedback(analytics)
    }

    private fun analytics(
        strength14: Double = 0.0,
        strength7: Double = 0.0,
        pushFreq14: Double = 0.0,
        recoveryBalance14: Double = 100.0,
        resistanceTrend5: Double = 0.0,
        avgEnergy7: Double = 4.0,
        burnoutTrigger: Boolean = false
    ) = IdentityAnalytics(
        dailyIdentityPoints = 0.0,
        strength14 = strength14,
        strength7 = strength7,
        pushFreq14 = pushFreq14,
        recoveryBalance14 = recoveryBalance14,
        resistanceTrend5 = resistanceTrend5,
        avgEnergy7 = avgEnergy7,
        burnoutTrigger = burnoutTrigger,
        momentum = 0.0
    )
}

private class FakeDailyLogRepository : DailyLogRepository {
    override fun observeLog(identityId: Long, date: LocalDate): Flow<DailyLog?> = emptyFlow()
    override fun observeLogsForDate(identityIds: List<Long>, date: LocalDate): Flow<List<DailyLog>> = emptyFlow()
    override suspend fun upsertLog(log: DailyLog) = Unit
    override suspend fun getLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog> =
        emptyList()
    override suspend fun getRecentLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog> =
        emptyList()
    override suspend fun deleteLogsForIdentity(identityId: Long) = Unit
}

private class FakeIdentityRepository(
    private val totalFloorMinutes: Int = 0
) : IdentityRepository {
    override fun observeFirstIdentityForExperiment(experimentId: Long): Flow<Identity?> = emptyFlow()
    override fun observeIdentitiesForExperiment(experimentId: Long): Flow<List<Identity>> = emptyFlow()
    override suspend fun getFirstIdentityForExperiment(experimentId: Long): Identity? = null
    override suspend fun getIdentitiesForExperiment(experimentId: Long): List<Identity> = emptyList()
    override suspend fun getIdentityById(identityId: Long): Identity? = null
    override suspend fun getIdentityCountForExperiment(experimentId: Long): Int = 0
    override suspend fun getTotalFloorMinutesForExperiment(experimentId: Long): Int = totalFloorMinutes
    override suspend fun createIdentity(identity: Identity): Long = 0
    override suspend fun updateIdentity(identity: Identity) = Unit
    override suspend fun deleteIdentity(identityId: Long) = Unit
    override suspend fun categoryExists(experimentId: Long, category: IdentityCategory): Boolean = false
}

private class FakeSettingsRepository(
    discretionaryTimeMinutes: Int? = 180
) : SettingsRepository {
    override val onboardingComplete: Flow<Boolean> = MutableStateFlow(true)
    override val discretionaryTimeMinutes: Flow<Int?> = MutableStateFlow(discretionaryTimeMinutes)
    override suspend fun completeOnboarding(discretionaryTimeMinutes: Int) = Unit
}
