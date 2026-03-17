package com.logan.project90.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logan.project90.data.local.dao.DailyLogDao
import com.logan.project90.data.local.dao.ExperimentDao
import com.logan.project90.data.local.dao.IdentityDao
import com.logan.project90.data.local.entity.DailyLogEntity
import com.logan.project90.data.local.entity.ExperimentEntity
import com.logan.project90.data.local.entity.IdentityEntity

@Database(
    entities = [ExperimentEntity::class, IdentityEntity::class, DailyLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun identityDao(): IdentityDao
    abstract fun dailyLogDao(): DailyLogDao
}
