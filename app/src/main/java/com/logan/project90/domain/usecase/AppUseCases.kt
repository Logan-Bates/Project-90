package com.logan.project90.domain.usecase

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.domain.model.AnalyticsActivityState
import com.logan.project90.domain.model.AnalyticsConsistencySummary
import com.logan.project90.domain.model.AnalyticsIdentitySummary
import com.logan.project90.domain.model.AnalyticsOverview
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.FeedbackMessage
import com.logan.project90.domain.model.FeedbackType
import com.logan.project90.domain.model.Identity
import com.logan.project90.domain.model.IdentityDetail
import com.logan.project90.domain.model.IdentityAnalytics
import com.logan.project90.domain.model.SaveLogResult
import com.logan.project90.domain.model.TodayIdentityCardModel
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

private const val MAX_IDENTITY_NAME_LENGTH = 50
private const val MAX_IDENTITY_STATEMENT_LENGTH = 160

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
        validateIdentityFields(name, statement, floorMinutes, pushMinutes, importanceWeight)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        if (identityRepository.getIdentityCountForExperiment(experimentId) >= 4) {
            return Result.failure(IllegalStateException(ValidationMessages.maxFourIdentities))
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

class UpdateIdentityUseCase(
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        identityId: Long,
        name: String,
        statement: String,
        category: IdentityCategory,
        floorMinutes: Int,
        pushMinutes: Int,
        importanceWeight: Int
    ): Result<String?> {
        validateIdentityFields(name, statement, floorMinutes, pushMinutes, importanceWeight)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        val existing = identityRepository.getIdentityById(identityId)
            ?: return Result.failure(IllegalStateException("Identity not found."))
        val occupiedByOther = identityRepository
            .getIdentitiesForExperiment(existing.experimentId)
            .any { it.id != identityId && it.category == category }
        if (occupiedByOther) {
            return Result.failure(IllegalStateException(ValidationMessages.oneIdentityPerCategory))
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
        identityRepository.updateIdentity(
            existing.copy(
                name = name.trim(),
                statement = statement.trim(),
                category = category,
                floorMinutes = floorMinutes,
                pushMinutes = pushMinutes,
                importanceWeight = importanceWeight
            )
        )
        return Result.success(warning)
    }
}

class DeleteIdentityUseCase(
    private val identityRepository: IdentityRepository
) {
    suspend operator fun invoke(identityId: Long): Result<Unit> {
        val existing = identityRepository.getIdentityById(identityId)
            ?: return Result.failure(IllegalStateException("Identity not found."))
        identityRepository.deleteIdentity(existing.id)
        return Result.success(Unit)
    }
}

class GetEditableIdentityUseCase(
    private val identityRepository: IdentityRepository,
    private val experimentRepository: ExperimentRepository
) {
    suspend operator fun invoke(identityId: Long?): Pair<Identity?, List<IdentityCategory>> {
        val experiment = experimentRepository.getFirstExperiment()
        val experimentId = experiment?.id ?: return null to IdentityCategory.entries
        val identities = identityRepository.getIdentitiesForExperiment(experimentId)
        val editingIdentity = identityId?.let { id -> identities.firstOrNull { it.id == id } }
        val usedByOthers = identities
            .filter { it.id != editingIdentity?.id }
            .map { it.category }
            .toSet()
        val availableCategories = IdentityCategory.entries.filterNot { it in usedByOthers }
        return editingIdentity to availableCategories
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

class GenerateFeedbackUseCase(
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun experimentFeedback(
        experiment: Experiment,
        identityCards: List<TodayIdentityCardModel>
    ): FeedbackMessage? {
        if (identityCards.isEmpty()) return null
        val discretionaryTime = settingsRepository.discretionaryTimeMinutes.first()
        val totalFloorMinutes = identityRepository.getTotalFloorMinutesForExperiment(experiment.id)
        val candidates = mutableListOf<FeedbackMessage>()

        identityCards
            .mapNotNull { identityRiskFeedback(it.analytics) }
            .minByOrNull { it.priority }
            ?.let { candidates += it }

        conflictFeedback(totalFloorMinutes, discretionaryTime)?.let { candidates += it }

        if (candidates.isEmpty()) {
            return identityCards
                .mapNotNull { identityFeedback(it.analytics) }
                .minByOrNull { it.priority }
        }

        return candidates.minByOrNull { it.priority }
    }

    fun identityFeedback(analytics: IdentityAnalytics): FeedbackMessage? =
        buildIdentityCandidates(analytics).minByOrNull { it.priority }

    private fun buildIdentityCandidates(analytics: IdentityAnalytics): List<FeedbackMessage> {
        val candidates = mutableListOf<FeedbackMessage>()

        if (analytics.burnoutTrigger) {
            candidates += FeedbackMessage(
                type = FeedbackType.BURNOUT_RISK,
                title = "Burnout risk is elevated",
                message = "Recent push volume is high for your current energy or resistance trend. Lean on the floor until the pattern feels steadier.",
                priority = 1
            )
        }

        if (!analytics.burnoutTrigger && analytics.recoveryBalance14 < 70.0) {
            candidates += FeedbackMessage(
                type = FeedbackType.RECOVERY_WARNING,
                title = "Recovery looks compressed",
                message = "Recent effort is asking for more recovery. Keep the floor stable and add push only when energy feels more reliable.",
                priority = 2
            )
        }

        if (!analytics.burnoutTrigger && analytics.recoveryBalance14 >= 70.0) {
            when {
                analytics.strength14 >= 70.0 && analytics.pushFreq14 < 20.0 -> {
                    candidates += FeedbackMessage(
                        type = FeedbackType.PUSH_GUIDANCE,
                        title = "Push is optional here",
                        message = "Floor consistency is strong. Increase push only if it feels sustainable.",
                        priority = 4
                    )
                }
                analytics.strength14 >= 50.0 &&
                    analytics.strength7 + 10.0 < analytics.strength14 &&
                    analytics.resistanceTrend5 > 0.0 -> {
                    candidates += FeedbackMessage(
                        type = FeedbackType.PUSH_GUIDANCE,
                        title = "Recent effort looks less steady",
                        message = "The last few days are carrying more friction than the two-week pattern. Re-center on the floor before adding more push.",
                        priority = 4
                    )
                }
            }
        }

        if (candidates.isEmpty() &&
            analytics.strength14 >= 70.0 &&
            analytics.recoveryBalance14 >= 80.0 &&
            !analytics.burnoutTrigger
        ) {
            candidates += FeedbackMessage(
                type = FeedbackType.POSITIVE_STEADY_STATE,
                title = "Current pace looks steady",
                message = "Your recent pattern looks sustainable. Keep the floor reliable and use push selectively.",
                priority = 5
            )
        }

        return candidates
    }

    private fun identityRiskFeedback(analytics: IdentityAnalytics): FeedbackMessage? =
        buildIdentityCandidates(analytics)
            .filter { it.type == FeedbackType.BURNOUT_RISK || it.type == FeedbackType.RECOVERY_WARNING }
            .minByOrNull { it.priority }

    private fun conflictFeedback(
        totalFloorMinutes: Int,
        discretionaryTime: Int?
    ): FeedbackMessage? {
        if (discretionaryTime == null || totalFloorMinutes <= discretionaryTime * 0.70) return null
        return FeedbackMessage(
            type = FeedbackType.IDENTITY_CONFLICT,
            title = "Floor load is high",
            message = "Your total floor targets are taking most of the available time. Simplify the floor before increasing intensity.",
            priority = 3
        )
    }
}

class GetTodaySliceUseCase(
    private val experimentRepository: ExperimentRepository,
    private val identityRepository: IdentityRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val analyticsUseCase: CalculateIdentityAnalyticsUseCase,
    private val feedbackUseCase: GenerateFeedbackUseCase
) {
    operator fun invoke(referenceDate: LocalDate): Flow<TodaySlice> {
        val experimentFlow = experimentRepository.observeFirstExperiment()
        val identitiesFlow = experimentFlow.flatMapLatest { experiment ->
            if (experiment == null) {
                flowOf(emptyList<Identity>())
            } else {
                identityRepository.observeIdentitiesForExperiment(experiment.id)
            }
        }
        val logFlow = identitiesFlow.flatMapLatest { identities ->
            if (identities.isEmpty()) {
                flowOf(emptyList<DailyLog>())
            } else {
                dailyLogRepository.observeLogsForDate(
                    identityIds = identities.map { it.id },
                    date = referenceDate
                )
            }
        }

        return combine(experimentFlow, identitiesFlow, logFlow) { experiment, identities, todayLogs ->
            val logsByIdentityId = todayLogs.associateBy { it.identityId }
            val identityCards = identities.map { identity ->
                val todayLog = logsByIdentityId[identity.id]
                val analytics = analyticsUseCase(identity, referenceDate, todayLog)
                TodayIdentityCardModel(
                    identity = identity,
                    todayLog = todayLog,
                    analytics = analytics,
                    feedback = feedbackUseCase.identityFeedback(analytics)
                )
            }
            val experimentFeedback = if (experiment != null) {
                feedbackUseCase.experimentFeedback(
                    experiment = experiment,
                    identityCards = identityCards
                )
            } else null
            TodaySlice(
                experiment = experiment,
                experimentFeedback = experimentFeedback,
                identityCards = identityCards
            )
        }
    }
}

class GetAnalyticsOverviewUseCase(
    private val experimentRepository: ExperimentRepository,
    private val identityRepository: IdentityRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val analyticsUseCase: CalculateIdentityAnalyticsUseCase
) {
    operator fun invoke(referenceDate: LocalDate): Flow<AnalyticsOverview> {
        val experimentFlow = experimentRepository.observeFirstExperiment()
        val identitiesFlow = experimentFlow.flatMapLatest { experiment ->
            if (experiment == null) {
                flowOf(emptyList())
            } else {
                identityRepository.observeIdentitiesForExperiment(experiment.id)
            }
        }
        val todayLogsFlow = identitiesFlow.flatMapLatest { identities ->
            if (identities.isEmpty()) {
                flowOf(emptyList())
            } else {
                dailyLogRepository.observeLogsForDate(identities.map { it.id }, referenceDate)
            }
        }

        return combine(experimentFlow, identitiesFlow, todayLogsFlow) { experiment, identities, _ ->
            val summaries = identities.map { identity ->
                buildIdentitySummary(
                    identity = identity,
                    referenceDate = referenceDate
                )
            }
            val totalWeight = identities.sumOf { it.importanceWeight }
            val weightedMomentum = if (totalWeight == 0) {
                0.0
            } else {
                summaries.sumOf { it.momentum * it.identity.importanceWeight } / totalWeight.toDouble()
            }
            AnalyticsOverview(
                experiment = experiment,
                weightedMomentum = weightedMomentum.coerceIn(0.0, 100.0),
                identityCount = identities.size,
                totalFloorMinutes = identities.sumOf { it.floorMinutes },
                identitySummaries = summaries
            )
        }
    }

    private suspend fun buildIdentitySummary(
        identity: Identity,
        referenceDate: LocalDate
    ): AnalyticsIdentitySummary {
        val logs14 = dailyLogRepository.getLogsInRange(
            identityId = identity.id,
            startDate = referenceDate.minusDays(13),
            endDate = referenceDate
        )
        val logsByDate = logs14.associateBy { it.logDate }
        val activeDates = boundedWindowDates(
            createdDate = identity.createdDate,
            referenceDate = referenceDate,
            lookbackDays = 14
        )
        val currentAnalytics = analyticsUseCase(
            identity = identity,
            referenceDate = referenceDate,
            todayLog = logsByDate[referenceDate]
        )
        val trendSnapshots = activeDates.map { day ->
            analyticsUseCase(
                identity = identity,
                referenceDate = day,
                todayLog = logsByDate[day]
            )
        }
        return AnalyticsIdentitySummary(
            identity = identity,
            strength14 = currentAnalytics.strength14,
            momentum = currentAnalytics.momentum,
            strengthTrend = trendSnapshots.map { it.strength14 },
            momentumTrend = trendSnapshots.map { it.momentum },
            consistencySummary = AnalyticsConsistencySummary(
                loggedDays = logs14.size,
                floorDays = logs14.count { it.status == IdentityStatus.FLOOR_PROTECTED },
                pushDays = logs14.count { it.status == IdentityStatus.PUSH_EXECUTED },
                recoveryBalance14 = currentAnalytics.recoveryBalance14
            ),
            recentActivity = boundedWindowDates(
                createdDate = identity.createdDate,
                referenceDate = referenceDate,
                lookbackDays = 7
            ).map { day ->
                when (logsByDate[day]?.status) {
                    null -> AnalyticsActivityState.NO_LOG
                    IdentityStatus.MISSED -> AnalyticsActivityState.MISSED
                    IdentityStatus.FLOOR_PROTECTED -> AnalyticsActivityState.FLOOR
                    IdentityStatus.PUSH_EXECUTED -> AnalyticsActivityState.PUSH
                }
            }
        )
    }
}

class GetIdentityDetailUseCase(
    private val identityRepository: IdentityRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val analyticsUseCase: CalculateIdentityAnalyticsUseCase,
    private val feedbackUseCase: GenerateFeedbackUseCase
) {
    suspend operator fun invoke(identityId: Long, referenceDate: LocalDate): IdentityDetail? {
        val identity = identityRepository.getIdentityById(identityId) ?: return null
        val todayLog = dailyLogRepository.getLogsInRange(identityId, referenceDate, referenceDate).firstOrNull()
        val analytics = analyticsUseCase(identity, referenceDate, todayLog)
        val recentLogs = dailyLogRepository.getRecentLogsInRange(
            identityId = identityId,
            startDate = referenceDate.minusDays(13),
            endDate = referenceDate
        )
        return IdentityDetail(
            identity = identity,
            analytics = analytics,
            recentLogs = recentLogs,
            feedback = listOfNotNull(feedbackUseCase.identityFeedback(analytics))
        )
    }
}

private fun validateIdentityFields(
    name: String,
    statement: String,
    floorMinutes: Int,
    pushMinutes: Int,
    importanceWeight: Int
): String? = when {
    name.isBlank() -> ValidationMessages.identityNameRequired
    name.trim().length > MAX_IDENTITY_NAME_LENGTH -> ValidationMessages.identityNameTooLong
    statement.isBlank() -> ValidationMessages.identityStatementRequired
    statement.trim().length > MAX_IDENTITY_STATEMENT_LENGTH -> ValidationMessages.identityStatementTooLong
    floorMinutes <= 0 || floorMinutes > 1440 -> ValidationMessages.minutesRange1To1440
    pushMinutes <= floorMinutes -> ValidationMessages.pushGreaterThanFloor
    pushMinutes > 1440 -> ValidationMessages.minutesRange1To1440
    pushMinutes > floorMinutes * 3 -> ValidationMessages.pushMaxThreeTimesFloor
    importanceWeight !in 1..3 -> ValidationMessages.range1To3
    else -> null
}

private fun boundedWindowDates(
    createdDate: LocalDate,
    referenceDate: LocalDate,
    lookbackDays: Int
): List<LocalDate> {
    val start = max(createdDate.toEpochDay(), referenceDate.minusDays(lookbackDays.toLong() - 1).toEpochDay())
    if (start > referenceDate.toEpochDay()) return emptyList()
    val startDate = LocalDate.ofEpochDay(start)
    return generateSequence(startDate) { date ->
        date.plusDays(1).takeIf { !it.isAfter(referenceDate) }
    }.toList()
}
