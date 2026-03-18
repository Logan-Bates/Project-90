package com.logan.project90.navigation

sealed class AppDestination(val route: String) {
    data object Welcome : AppDestination("welcome")
    data object TimeBudget : AppDestination("time_budget")
    data object CreateExperiment : AppDestination("create_experiment")
    data object PresetSelection : AppDestination("preset_selection")
    data object CreateIdentity : AppDestination("create_identity?presetId={presetId}&identityId={identityId}") {
        fun route(presetId: String? = null, identityId: Long? = null): String {
            val parts = mutableListOf<String>()
            if (!presetId.isNullOrBlank()) parts += "presetId=$presetId"
            if (identityId != null) parts += "identityId=$identityId"
            return if (parts.isEmpty()) "create_identity" else "create_identity?${parts.joinToString("&")}"
        }
    }
    data object ManageIdentities : AppDestination("manage_identities")
    data object IdentityDetail : AppDestination("identity_detail/{identityId}") {
        fun route(identityId: Long): String = "identity_detail/$identityId"
    }
    data object Today : AppDestination("today")
}
