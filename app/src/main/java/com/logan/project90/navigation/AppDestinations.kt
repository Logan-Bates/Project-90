package com.logan.project90.navigation

sealed class AppDestination(val route: String) {
    data object Welcome : AppDestination("welcome")
    data object TimeBudget : AppDestination("time_budget")
    data object CreateExperiment : AppDestination("create_experiment")
    data object PresetSelection : AppDestination("preset_selection")
    data object ManageIdentitiesSetup : AppDestination("manage_identities_setup")
    data object Main : AppDestination("main")
    data object Today : AppDestination("today")
    data object Analytics : AppDestination("analytics")
    data object Experiment : AppDestination("experiment")
    data object CreateIdentity : AppDestination("create_identity?presetId={presetId}&identityId={identityId}") {
        fun route(presetId: String? = null, identityId: Long? = null): String {
            val parts = mutableListOf<String>()
            if (!presetId.isNullOrBlank()) parts += "presetId=$presetId"
            if (identityId != null) parts += "identityId=$identityId"
            return if (parts.isEmpty()) "create_identity" else "create_identity?${parts.joinToString("&")}"
        }
    }
    data object IdentityDetail : AppDestination("identity_detail/{identityId}") {
        fun route(identityId: Long): String = "identity_detail/$identityId"
    }

    companion object {
        val topLevelDestinations = listOf(Analytics, Today, Experiment)
    }
}
