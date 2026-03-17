package com.logan.project90.domain.usecase

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.Identity
import com.logan.project90.domain.model.IdentityAnalytics
import com.logan.project90.domain.model.SaveLogResult
import com.logan.project90.domain.model.TodaySlice
import com.logan.project90.domain.repository.DailyLogRepository
import com.logan.project90.domain.repository.ExperimentRepository
import com.logan.project90.domain.repository.IdentityRepository
import com.logan.project90.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import kotlin.math.max

class CompleteOnboardingUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(discretionaryTimeMinutes: Int) {
        require(discretionaryTimeMinutes in 1..1440)
        settingsRepository.completeOnboarding(discretionaryTimeMinutes)
    }
}

class CreateExperimentUseCase(
    private val experimentRepository: ExperimentRepository
) {
    suspend operator fun invoke(
        name: String,
        startDate: LocalDate,
        durationDays: Int = 90
    ): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException(ValidationMessages.experimentNameRequired))
        if (experimentRepository.getFirstExperiment() != null) {
            return Result.failure(IllegalStateException("This slice supports exactly one experiment in the UI."))
        }
        val experiment = Experiment(
            name = name.trim(),
            startDate = startDate,
            durationDays = durationDays,
            endDate = startDate.plusDays(durationDays.toLong() - 1)
        )
        return Result.success(experimentRepository.createExperiment(experiment))
    }
}

class CreateIdentityUseCase(
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        experimentId: Long,
        name: String,
        statement: String,
        category: IdentityCategory,
        floorMinutes: Int,
        pushMinutes: Int,
        importanceWeight: Int,
        createdDate: LocalDate
    ): Result<Pair<Long, String?>> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException(ValidationMessages.identityNameRequired))
        if (statement.isBlank()) return Result.failure(IllegalArgumentException(ValidationMessages.identityStatementRequired))
        if (floorMinutes <= 0) return Result.failure(IllegalArgumentException(ValidationMessages.minutesRange1To1440))
        if (pushMinutes <= floorMinutes) {
            return Result.failure(IllegalArgumentException(ValidationMessages.pushGreaterThanFloor))
        }
        if (pushMinutes > floorMinutes * 3) {
            return Result.failure(IllegalArgumentException(ValidationMessages.pushMaxThreeTimesFloor))
        }
        if (importanceWeight !in 1..3) {
            return Result.failure(IllegalArgumentException(ValidationMessages.range1To3))
        }
        if (identityRepository.categoryExists(experimentId, category)) {
            return Result.failure(
                IllegalStateException(ValidationMessages.oneIdentityPerCategory)
            )
        }

        val available = settingsRepository.discretionaryTimeMinutes.first()
        val warning = buildString {
            if (floorMinutes > 90) {
                append(ValidationMessages.floorBurnoutRisk)
            }
            if (available != null && floorMinutes > available / 2) {
                if (isNotEmpty()) append("\n")
                append(ValidationMessages.floorOverTimeBudget)
            }
        }.ifBlank { null }

        val identity = Identity(
            experimentId = experimentId,
            name = name.trim(),
            statement = statement.trim(),
            category = category,
            floorMinutes = floorMinutes,
            pushMinutes = pushMinutes,
            importanceWeight = importanceWeight,
            createdDate = createdDate
        )
        return Result.success(identityRepository.createIdentity(identity) to warning)
    }
}

class LogIdentityDayUseCase(
    private val dailyLogRepository: DailyLogRepository
) {
    suspend operator fun invoke(
        identity: Identity,
        logDate: LocalDate,
        effortMinutes: Int,
        status: IdentityStatus,
        energy: Int,
        mood: Int,
        resistance: ResistanceLevel,
        reflection: String
    ): Result<SaveLogResult> {
        if (effortMinutes < 0) return Result.failure(IllegalArgumentException(ValidationMessages.effortRange0To1440))
        if (energy !in 1..5) return Result.failure(IllegalArgumentException(ValidationMessages.range1To5))
        if (mood !in 1..5) return Result.failure(IllegalArgumentException(ValidationMessages.range1To5))

        val warning = when {
            status == IdentityStatus.MISSED && effortMinutes > 0 ->
                ValidationMessages.missedCannotIncludeEffort
            status == IdentityStatus.FLOOR_PROTECTED && effortMinutes < identity.floorMinutes ->
                ValidationMessages.floorProtectedBelowFloor
            status == IdentityStatus.FLOOR_PROTECTED && effortMinutes >= identity.pushMinutes ->
                ValidationMessages.floorProtectedAtPush
            status == IdentityStatus.PUSH_EXECUTED && effortMinutes < identity.pushMinutes ->
                ValidationMessages.pushExecutedBelowPush
            else -> null
        }

        dailyLogRepository.upsertLog(
            DailyLog(
                identityId = identity.id,
                logDate = logDate,
                effortMinutes = effortMinutes,
                status = status,
                energy = energy,
                mood = mood,
                resistance = resistance,
                reflection = reflection.trim()
            )
        )

        return Result.success(SaveLogResult(warning = warning))
    }
}

class CalculateIdentityAnalyticsUseCase(
    private val dailyLogRepository: DailyLogRepository
) {
    suspend operator fun invoke(
        identity: Identity,
        referenceDate: LocalDate,
        todayLog: DailyLog?
    ): IdentityAnalytics {
        val logs14 = dailyLogRepository.getLogsInRange(identity.id, referenceDate.minusDays(13), referenceDate)
        val logs7 = dailyLogRepository.getLogsInRange(identity.id, referenceDate.minusDays(6), referenceDate)
        val logs5 = dailyLogRepository.getLogsInRange(identity.id, referenceDate.minusDays(4), referenceDate)

        val activeDays14 = activeDaysInWindow(identity.createdDate, referenceDate, 14)
        val activeDays7 = activeDaysInWindow(identity.createdDate, referenceDate, 7)
        val pushDays14 = logs14.count { it.status == IdentityStatus.PUSH_EXECUTED }
        val pushDays7 = logs7.count { it.status == IdentityStatus.PUSH_EXECUTED }
        val avgEnergy7 = if (logs7.isEmpty()) 5.0 else logs7.map { it.energy }.average()
        val resistanceTrend5 = calculateResistanceTrend(logs5)
        val recoveryBalance14 = calculateRecoveryBalance14(pushDays14, activeDays14, avgEnergy7, resistanceTrend5)
        val burnoutTrigger = pushDays7 >= 5 && (avgEnergy7 <= 2.5 || resistanceTrend5 > 0)
        val strength14 = calculateStrength(logs14, activeDays14)
        val strength7 = calculateStrength(logs7, activeDays7)
        val pushFreq14 = calculatePushFreq14(pushDays14, activeDays14)
        val momentum = calculateMomentum(strength7, pushFreq14, recoveryBalance14, burnoutTrigger)

        return IdentityAnalytics(
            dailyIdentityPoints = todayLog?.let(::dailyIdentityPoints) ?: 0.0,
            strength14 = strength14,
            strength7 = strength7,
            pushFreq14 = pushFreq14,
            recoveryBalance14 = recoveryBalance14,
            resistanceTrend5 = resistanceTrend5,
            avgEnergy7 = avgEnergy7,
            burnoutTrigger = burnoutTrigger,
            momentum = momentum
        )
    }

    internal fun dailyIdentityPoints(log: DailyLog): Double {
        val base = when (log.status) {
            IdentityStatus.MISSED -> 0.0
            IdentityStatus.FLOOR_PROTECTED -> 1.0
            IdentityStatus.PUSH_EXECUTED -> 1.5
        }
        val bonus = if (log.status == IdentityStatus.MISSED) {
            0.0
        } else {
            when (log.resistance) {
                ResistanceLevel.NONE -> 0.0
                ResistanceLevel.MILD -> 0.05
                ResistanceLevel.MODERATE -> 0.15
                ResistanceLevel.HIGH -> 0.25
            }
        }
        return (base + bonus).coerceAtMost(1.75)
    }

    internal fun calculateStrength(logs: List<DailyLog>, activeDays: Int): Double {
        if (activeDays <= 0) return 0.0
        val score = logs.sumOf(::dailyIdentityPoints) / (activeDays * 1.5) * 100.0
        return score.coerceIn(0.0, 100.0)
    }

    internal fun calculatePushFreq14(pushDays14: Int, activeDays14: Int): Double {
        if (activeDays14 <= 0) return 0.0
        return (pushDays14.toDouble() / activeDays14.toDouble() * 100.0).coerceIn(0.0, 100.0)
    }

    internal fun calculateRecoveryBalance14(
        pushDays14: Int,
        activeDays14: Int,
        avgEnergy7: Double,
        resistanceTrend5: Double
    ): Double {
        if (activeDays14 <= 0) return 100.0
        val pushRatio14 = pushDays14.toDouble() / activeDays14.toDouble()
        var recovery = if (pushRatio14 <= 0.60) {
            100.0
        } else {
            100.0 * (1.0 - (pushRatio14 - 0.60) / 0.40)
        }
        if (avgEnergy7 <= 2.5) recovery *= 0.85
        if (resistanceTrend5 > 0) recovery *= 0.85
        return recovery.coerceIn(0.0, 100.0)
    }

    internal fun calculateMomentum(
        strength7: Double,
        pushFreq14: Double,
        recoveryBalance14: Double,
        burnoutTrigger: Boolean
    ): Double {
        val momentum = if (burnoutTrigger) {
            (0.40 * strength7) + (0.20 * pushFreq14) + (0.40 * recoveryBalance14)
        } else {
            (0.50 * strength7) + (0.20 * pushFreq14) + (0.30 * recoveryBalance14)
        }
        return momentum.coerceIn(0.0, 100.0)
    }

    internal fun calculateResistanceTrend(logs5: List<DailyLog>): Double {
        if (logs5.size < 2) return 0.0
        val sorted = logs5.sortedBy { it.logDate }
        val xs = sorted.indices.map { it.toDouble() }
        val ys = sorted.map { it.resistance.score.toDouble() }
        val xMean = xs.average()
        val yMean = ys.average()
        val numerator = xs.indices.sumOf { (xs[it] - xMean) * (ys[it] - yMean) }
        val denominator = xs.sumOf { (it - xMean) * (it - xMean) }
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    internal fun activeDaysInWindow(createdDate: LocalDate, referenceDate: LocalDate, lookbackDays: Int): Int {
        val windowStart = referenceDate.minusDays(lookbackDays.toLong() - 1)
        val activeStart = max(createdDate.toEpochDay(), windowStart.toEpochDay())
        val days = referenceDate.toEpochDay() - activeStart + 1
        return max(0L, days).toInt()
    }
}

class GetTodaySliceUseCase(
    private val experimentRepository: ExperimentRepository,
    private val identityRepository: IdentityRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val analyticsUseCase: CalculateIdentityAnalyticsUseCase
) {
    operator fun invoke(referenceDate: LocalDate): Flow<TodaySlice> {
        val experimentFlow = experimentRepository.observeFirstExperiment()
        val identityFlow = experimentFlow.flatMapLatest { experiment ->
            if (experiment == null) {
                flowOf(null)
            } else {
                identityRepository.observeFirstIdentityForExperiment(experiment.id)
            }
        }
        val logFlow = identityFlow.flatMapLatest { identity ->
            if (identity == null) {
                flowOf(null)
            } else {
                dailyLogRepository.observeLog(identity.id, referenceDate)
            }
        }

        return combine(experimentFlow, identityFlow, logFlow) { experiment, identity, todayLog ->
            val analytics = if (identity != null) {
                analyticsUseCase(identity, referenceDate, todayLog)
            } else {
                null
            }
            TodaySlice(
                experiment = experiment,
                identity = identity,
                todayLog = todayLog,
                analytics = analytics
            )
        }
    }
}
