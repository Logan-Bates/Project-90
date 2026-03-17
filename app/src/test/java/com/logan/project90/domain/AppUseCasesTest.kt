package com.logan.project90.domain

import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.repository.DailyLogRepository
import com.logan.project90.domain.usecase.CalculateIdentityAnalyticsUseCase
import kotlinx.coroutines.flow.Flow
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
}

private class FakeDailyLogRepository : DailyLogRepository {
    override fun observeLog(identityId: Long, date: LocalDate): Flow<DailyLog?> = emptyFlow()
    override suspend fun upsertLog(log: DailyLog) = Unit
    override suspend fun getLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog> =
        emptyList()
}
