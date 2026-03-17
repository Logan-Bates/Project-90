package com.logan.project90.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDateEpochDay: Long,
    val durationDays: Int,
    val endDateEpochDay: Long
)

@Entity(
    tableName = "identities",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experimentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["experimentId"])]
)
data class IdentityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentId: Long,
    val name: String,
    val statement: String,
    val category: String,
    val floorMinutes: Int,
    val pushMinutes: Int,
    val importanceWeight: Int,
    val createdDateEpochDay: Long
)

@Entity(
    tableName = "daily_logs",
    foreignKeys = [
        ForeignKey(
            entity = IdentityEntity::class,
            parentColumns = ["id"],
            childColumns = ["identityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["identityId", "logDateEpochDay"], unique = true)]
)
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identityId: Long,
    val logDateEpochDay: Long,
    val effortMinutes: Int,
    val status: String,
    val energy: Int,
    val mood: Int,
    val resistance: String,
    val reflection: String
)
