package com.logan.project90.core.model

enum class IdentityCategory {
    MIND,
    BODY,
    SKILL,
    EMOTIONAL,
    CUSTOM
}

enum class IdentityStatus {
    MISSED,
    FLOOR_PROTECTED,
    PUSH_EXECUTED
}

enum class ResistanceLevel(val score: Int) {
    NONE(0),
    MILD(1),
    MODERATE(2),
    HIGH(3)
}
