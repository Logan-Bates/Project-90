package com.logan.project90.navigation

sealed class AppDestination(val route: String) {
    data object Welcome : AppDestination("welcome")
    data object TimeBudget : AppDestination("time_budget")
    data object CreateExperiment : AppDestination("create_experiment")
    data object CreateIdentity : AppDestination("create_identity")
    data object Today : AppDestination("today")
}
