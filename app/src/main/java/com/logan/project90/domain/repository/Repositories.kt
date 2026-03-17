package com.logan.project90.domain.repository

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.Identity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingsRepository {
    val onboardingComplete: Flow<Boolean>
    val discretionaryTimeMinutes: Flow<Int?>
    suspend fun completeOnboarding(discretionaryTimeMinutes: Int)
}

interface ExperimentRepository {
    fun observeFirstExperiment(): Flow<Experiment?>
    suspend fun getFirstExperiment(): Experiment?
    suspend fun createExperiment(experiment: Experiment): Long
}

interface IdentityRepository {
    fun observeFirstIdentityForExperiment(experimentId: Long): Flow<Identity?>
    suspend fun getFirstIdentityForExperiment(experimentId: Long): Identity?
    suspend fun getIdentityById(identityId: Long): Identity?
    suspend fun getTotalFloorMinutesForExperiment(experimentId: Long): Int
    suspend fun createIdentity(identity: Identity): Long
    suspend fun categoryExists(experimentId: Long, category: IdentityCategory): Boolean
}

interface DailyLogRepository {
    fun observeLog(identityId: Long, date: LocalDate): Flow<DailyLog?>
    suspend fun upsertLog(log: DailyLog)
    suspend fun getLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog>
    suspend fun getRecentLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog>
}
