package com.logan.project90.data.repository

import com.logan.project90.core.model.IdentityCategory
import com.logan.project90.core.model.IdentityStatus
import com.logan.project90.core.model.ResistanceLevel
import com.logan.project90.core.util.toEpochDayLong
import com.logan.project90.core.util.toLocalDate
import com.logan.project90.data.local.dao.DailyLogDao
import com.logan.project90.data.local.dao.ExperimentDao
import com.logan.project90.data.local.dao.IdentityDao
import com.logan.project90.data.local.entity.DailyLogEntity
import com.logan.project90.data.local.entity.ExperimentEntity
import com.logan.project90.data.local.entity.IdentityEntity
import com.logan.project90.domain.model.DailyLog
import com.logan.project90.domain.model.Experiment
import com.logan.project90.domain.model.Identity
import com.logan.project90.domain.repository.DailyLogRepository
import com.logan.project90.domain.repository.ExperimentRepository
import com.logan.project90.domain.repository.IdentityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExperimentRepositoryImpl(
    private val dao: ExperimentDao
) : ExperimentRepository {
    override fun observeFirstExperiment(): Flow<Experiment?> =
        dao.observeFirstExperiment().map { it?.toDomain() }

    override suspend fun getFirstExperiment(): Experiment? = dao.getFirstExperiment()?.toDomain()

    override suspend fun createExperiment(experiment: Experiment): Long = dao.insert(experiment.toEntity())
}

class IdentityRepositoryImpl(
    private val dao: IdentityDao
) : IdentityRepository {
    override fun observeFirstIdentityForExperiment(experimentId: Long): Flow<Identity?> =
        dao.observeFirstIdentityForExperiment(experimentId).map { it?.toDomain() }

    override suspend fun getFirstIdentityForExperiment(experimentId: Long): Identity? =
        dao.getFirstIdentityForExperiment(experimentId)?.toDomain()

    override suspend fun createIdentity(identity: Identity): Long = dao.insert(identity.toEntity())

    override suspend fun categoryExists(experimentId: Long, category: IdentityCategory): Boolean =
        dao.categoryExists(experimentId, category.name)
}

class DailyLogRepositoryImpl(
    private val dao: DailyLogDao
) : DailyLogRepository {
    override fun observeLog(identityId: Long, date: LocalDate): Flow<DailyLog?> =
        dao.observeLog(identityId, date.toEpochDayLong()).map { it?.toDomain() }

    override suspend fun upsertLog(log: DailyLog) {
        dao.upsert(log.toEntity())
    }

    override suspend fun getLogsInRange(identityId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyLog> =
        dao.getLogsInRange(identityId, startDate.toEpochDayLong(), endDate.toEpochDayLong()).map { it.toDomain() }
}

private fun ExperimentEntity.toDomain() = Experiment(
    id = id,
    name = name,
    startDate = startDateEpochDay.toLocalDate(),
    durationDays = durationDays,
    endDate = endDateEpochDay.toLocalDate()
)

private fun Experiment.toEntity() = ExperimentEntity(
    id = id,
    name = name,
    startDateEpochDay = startDate.toEpochDayLong(),
    durationDays = durationDays,
    endDateEpochDay = endDate.toEpochDayLong()
)

private fun IdentityEntity.toDomain() = Identity(
    id = id,
    experimentId = experimentId,
    name = name,
    statement = statement,
    category = IdentityCategory.valueOf(category),
    floorMinutes = floorMinutes,
    pushMinutes = pushMinutes,
    importanceWeight = importanceWeight,
    createdDate = createdDateEpochDay.toLocalDate()
)

private fun Identity.toEntity() = IdentityEntity(
    id = id,
    experimentId = experimentId,
    name = name,
    statement = statement,
    category = category.name,
    floorMinutes = floorMinutes,
    pushMinutes = pushMinutes,
    importanceWeight = importanceWeight,
    createdDateEpochDay = createdDate.toEpochDayLong()
)

private fun DailyLogEntity.toDomain() = DailyLog(
    id = id,
    identityId = identityId,
    logDate = logDateEpochDay.toLocalDate(),
    effortMinutes = effortMinutes,
    status = IdentityStatus.valueOf(status),
    energy = energy,
    mood = mood,
    resistance = ResistanceLevel.valueOf(resistance),
    reflection = reflection
)

private fun DailyLog.toEntity() = DailyLogEntity(
    id = id,
    identityId = identityId,
    logDateEpochDay = logDate.toEpochDayLong(),
    effortMinutes = effortMinutes,
    status = status.name,
    energy = energy,
    mood = mood,
    resistance = resistance.name,
    reflection = reflection
)
