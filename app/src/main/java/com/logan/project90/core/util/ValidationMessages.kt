package com.logan.project90.core.util

object ValidationMessages {
    const val experimentNameRequired = "Experiment name is required."
    const val identityNameRequired = "Identity name is required."
    const val identityStatementRequired = "Identity statement is required."
    const val minutesRange1To1440 = "Enter 1-1,440 minutes."
    const val effortRange0To1440 = "Enter 0-1,440 minutes."
    const val range1To5 = "Enter 1-5."
    const val range1To3 = "Enter 1-3."
    const val pushGreaterThanFloor = "Push must be greater than Floor."
    const val pushMaxThreeTimesFloor = "Push cannot exceed 3x Floor."
    const val oneIdentityPerCategory = "Only one identity per category."
    const val floorBurnoutRisk = "Lower the Floor to reduce burnout risk."
    const val floorOverTimeBudget = "Floor exceeds 50% of time budget."
    const val createIdentityBeforeLogging = "Create an identity before logging."
    const val missedCannotIncludeEffort = "Missed cannot include effort minutes."
    const val floorProtectedBelowFloor = "Floor Protected is below Floor."
    const val floorProtectedAtPush = "Use Push Executed for Push-level effort."
    const val pushExecutedBelowPush = "Push Executed is below Push."
}
