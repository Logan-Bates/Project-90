package com.logan.project90.navigation

sealed class AppDestination(val route: String) {
    data object Welcome : AppDestination("welcome")
    data object TimeBudget : AppDestination("time_budget")
    data object CreateExperiment : AppDestination("create_experiment")
    data object PresetSelection : AppDestination("preset_selection")
    data object CreateIdentity : AppDestination("create_identity?presetId={presetId}") {
        fun route(presetId: String? = null): String =
            if (presetId.isNullOrBlank()) "create_identity"
            else "create_identity?presetId=$presetId"
    }
    data object IdentityDetail : AppDestination("identity_detail/{identityId}") {
        fun route(identityId: Long): String = "identity_detail/$identityId"
    }
    data object Today : AppDestination("today")
}
