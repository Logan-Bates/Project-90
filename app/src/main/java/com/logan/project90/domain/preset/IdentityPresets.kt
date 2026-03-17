package com.logan.project90.domain.preset

import com.logan.project90.core.model.IdentityCategory

data class IdentityPreset(
    val id: String,
    val name: String,
    val category: IdentityCategory,
    val description: String,
    val defaultStatement: String,
    val defaultFloorMinutes: Int,
    val defaultPushMinutes: Int,
    val defaultWeight: Int
)

object IdentityPresets {
    val all: List<IdentityPreset> = listOf(
        IdentityPreset(
            id = "deep_work",
            name = "Deep Work",
            category = IdentityCategory.MIND,
            description = "Focused, uninterrupted cognitive output.",
            defaultStatement = "I am someone who protects focused work every day.",
            defaultFloorMinutes = 45,
            defaultPushMinutes = 90,
            defaultWeight = 3
        ),
        IdentityPreset(
            id = "personal_care",
            name = "Personal Care",
            category = IdentityCategory.BODY,
            description = "Intentional self-care and hygiene maintenance.",
            defaultStatement = "I am someone who takes care of myself daily.",
            defaultFloorMinutes = 20,
            defaultPushMinutes = 45,
            defaultWeight = 2
        ),
        IdentityPreset(
            id = "project_progress",
            name = "Project Progress",
            category = IdentityCategory.SKILL,
            description = "Incremental, visible progress on a defined project.",
            defaultStatement = "I am someone who moves meaningful work forward daily.",
            defaultFloorMinutes = 30,
            defaultPushMinutes = 90,
            defaultWeight = 3
        ),
        IdentityPreset(
            id = "emotional_regulation",
            name = "Emotional Regulation",
            category = IdentityCategory.EMOTIONAL,
            description = "Intentional reflection and emotional processing.",
            defaultStatement = "I am someone who processes emotions with intention.",
            defaultFloorMinutes = 10,
            defaultPushMinutes = 30,
            defaultWeight = 2
        )
    )

    fun findById(id: String?): IdentityPreset? = all.firstOrNull { it.id == id }
}
