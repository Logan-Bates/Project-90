package com.logan.project90.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.logan.project90.data.local.entity.DailyLogEntity
import com.logan.project90.data.local.entity.ExperimentEntity
import com.logan.project90.data.local.entity.IdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiments ORDER BY id ASC LIMIT 1")
    fun observeFirstExperiment(): Flow<ExperimentEntity?>

    @Query("SELECT * FROM experiments ORDER BY id ASC LIMIT 1")
    suspend fun getFirstExperiment(): ExperimentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(experiment: ExperimentEntity): Long
}

@Dao
interface IdentityDao {
    @Query("SELECT * FROM identities WHERE experimentId = :experimentId ORDER BY id ASC LIMIT 1")
    fun observeFirstIdentityForExperiment(experimentId: Long): Flow<IdentityEntity?>

    @Query("SELECT * FROM identities WHERE experimentId = :experimentId ORDER BY id ASC")
    fun observeIdentitiesForExperiment(experimentId: Long): Flow<List<IdentityEntity>>

    @Query("SELECT * FROM identities WHERE experimentId = :experimentId ORDER BY id ASC LIMIT 1")
    suspend fun getFirstIdentityForExperiment(experimentId: Long): IdentityEntity?

    @Query("SELECT * FROM identities WHERE experimentId = :experimentId ORDER BY id ASC")
    suspend fun getIdentitiesForExperiment(experimentId: Long): List<IdentityEntity>

    @Query("SELECT * FROM identities WHERE id = :identityId LIMIT 1")
    suspend fun getIdentityById(identityId: Long): IdentityEntity?

    @Query("SELECT COUNT(*) FROM identities WHERE experimentId = :experimentId")
    suspend fun getIdentityCountForExperiment(experimentId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM identities WHERE experimentId = :experimentId AND category = :category)")
    suspend fun categoryExists(experimentId: Long, category: String): Boolean

    @Query("SELECT COALESCE(SUM(floorMinutes), 0) FROM identities WHERE experimentId = :experimentId")
    suspend fun getTotalFloorMinutesForExperiment(experimentId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(identity: IdentityEntity): Long

    @Update
    suspend fun update(identity: IdentityEntity)

    @Query("DELETE FROM identities WHERE id = :identityId")
    suspend fun deleteById(identityId: Long)
}

@Dao
interface DailyLogDao {
    @Query(
        """
        SELECT * FROM daily_logs
        WHERE identityId = :identityId AND logDateEpochDay = :logDateEpochDay
        LIMIT 1
        """
    )
    fun observeLog(identityId: Long, logDateEpochDay: Long): Flow<DailyLogEntity?>

    @Query(
        """
        SELECT * FROM daily_logs
        WHERE identityId IN (:identityIds) AND logDateEpochDay = :logDateEpochDay
        ORDER BY identityId ASC
        """
    )
    fun observeLogsForDate(identityIds: List<Long>, logDateEpochDay: Long): Flow<List<DailyLogEntity>>

    @Query(
        """
        SELECT * FROM daily_logs
        WHERE identityId = :identityId
        AND logDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY logDateEpochDay ASC
        """
    )
    suspend fun getLogsInRange(identityId: Long, startEpochDay: Long, endEpochDay: Long): List<DailyLogEntity>

    @Query(
        """
        SELECT * FROM daily_logs
        WHERE identityId = :identityId
        AND logDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY logDateEpochDay DESC
        """
    )
    suspend fun getRecentLogsInRange(identityId: Long, startEpochDay: Long, endEpochDay: Long): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE identityId = :identityId")
    suspend fun deleteByIdentityId(identityId: Long)
}
