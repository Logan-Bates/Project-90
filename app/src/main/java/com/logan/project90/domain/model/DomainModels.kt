package com.logan.project90.domain.model

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import java.time.LocalDate

data class Experiment(
    val id: Long = 0,
    val name: String,
    val startDate: LocalDate,
    val durationDays: Int,
    val endDate: LocalDate
)

data class Identity(
    val id: Long = 0,
    val experimentId: Long,
    val name: String,
    val statement: String,
    val category: IdentityCategory,
    val floorMinutes: Int,
    val pushMinutes: Int,
    val importanceWeight: Int,
    val createdDate: LocalDate
)

data class DailyLog(
    val id: Long = 0,
    val identityId: Long,
    val logDate: LocalDate,
    val effortMinutes: Int,
    val status: IdentityStatus,
    val energy: Int,
    val mood: Int,
    val resistance: ResistanceLevel,
    val reflection: String
)

data class IdentityAnalytics(
    val dailyIdentityPoints: Double,
    val strength14: Double,
    val strength7: Double,
    val pushFreq14: Double,
    val recoveryBalance14: Double,
    val resistanceTrend5: Double,
    val avgEnergy7: Double,
    val burnoutTrigger: Boolean,
    val momentum: Double
)

data class TodaySlice(
    val experiment: Experiment?,
    val identity: Identity?,
    val todayLog: DailyLog?,
    val analytics: IdentityAnalytics?
)

data class SaveLogResult(
    val warning: String?
)
