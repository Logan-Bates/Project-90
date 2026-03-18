package com.logan.project90.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.analytics.AnalyticsScreen
import com.logan.project90.ui.analytics.AnalyticsViewModel
import com.logan.project90.ui.components.AppBottomBar
import com.logan.project90.ui.experiment.CreateExperimentScreen
import com.logan.project90.ui.experiment.CreateExperimentViewModel
import com.logan.project90.ui.identity.CreateIdentityScreen
import com.logan.project90.ui.identity.CreateIdentityViewModel
import com.logan.project90.ui.identity.IdentityDetailScreen
import com.logan.project90.ui.identity.IdentityDetailViewModel
import com.logan.project90.ui.identity.ManageIdentitiesScreen
import com.logan.project90.ui.identity.ManageIdentitiesViewModel
import com.logan.project90.ui.identity.PresetSelectionScreen
import com.logan.project90.ui.identity.PresetSelectionViewModel
import com.logan.project90.ui.timebudget.TimeBudgetScreen
import com.logan.project90.ui.timebudget.TimeBudgetViewModel
import com.logan.project90.ui.today.TodayScreen
import com.logan.project90.ui.today.TodayViewModel
import com.logan.project90.ui.welcome.WelcomeScreen
import com.logan.project90.ui.welcome.WelcomeViewModel

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination.isTopLevelDestination()

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        navController.navigateToTopLevel(destination)
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            appContainer = appContainer,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Welcome.route,
        modifier = modifier
    ) {
        composable(AppDestination.Welcome.route) {
            val viewModel: WelcomeViewModel = viewModel(factory = WelcomeViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WelcomeScreen(
                uiState = uiState,
                onContinue = {
                    val target = when {
                        !uiState.onboardingComplete -> AppDestination.TimeBudget.route
                        !uiState.hasExperiment -> AppDestination.CreateExperiment.route
                        else -> AppDestination.Main.route
                    }
                    if (target == AppDestination.Main.route) {
                        navController.navigate(target) {
                            popUpTo(AppDestination.Welcome.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(target)
                    }
                }
            )
        }
        composable(AppDestination.TimeBudget.route) {
            val viewModel: TimeBudgetViewModel = viewModel(factory = TimeBudgetViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TimeBudgetScreen(
                uiState = uiState,
                onMinutesChanged = viewModel::updateMinutes,
                onSave = {
                    viewModel.save {
                        navController.navigate(AppDestination.CreateExperiment.route)
                    }
                }
            )
        }
        composable(AppDestination.CreateExperiment.route) {
            val viewModel: CreateExperimentViewModel =
                viewModel(factory = CreateExperimentViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CreateExperimentScreen(
                uiState = uiState,
                onNameChanged = viewModel::updateName,
                onCreate = {
                    viewModel.createExperiment {
                        navController.navigate(AppDestination.PresetSelection.route)
                    }
                }
            )
        }
        composable(AppDestination.PresetSelection.route) {
            val viewModel: PresetSelectionViewModel =
                viewModel(factory = PresetSelectionViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            PresetSelectionScreen(
                uiState = uiState,
                onPresetSelected = { presetId ->
                    navController.navigate(AppDestination.CreateIdentity.route(presetId))
                },
                onCreateCustom = {
                    navController.navigate(AppDestination.CreateIdentity.route())
                }
            )
        }
        composable(
            route = AppDestination.CreateIdentity.route,
            arguments = listOf(
                navArgument("presetId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("identityId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getString("presetId").orEmpty().ifBlank { null }
            val identityId = backStackEntry.arguments?.getLong("identityId")?.takeIf { it != 0L }
            val viewModel: CreateIdentityViewModel =
                viewModel(factory = CreateIdentityViewModel.factory(appContainer, presetId, identityId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CreateIdentityScreen(
                uiState = uiState,
                onNameChanged = viewModel::updateName,
                onStatementChanged = viewModel::updateStatement,
                onCategoryChanged = viewModel::updateCategory,
                onFloorChanged = viewModel::updateFloorMinutes,
                onPushChanged = viewModel::updatePushMinutes,
                onWeightChanged = viewModel::updateWeight,
                onSave = {
                    viewModel.saveIdentity {
                        when {
                            navController.popBackStack(AppDestination.ManageIdentitiesSetup.route, false) -> Unit
                            navController.popBackStack(AppDestination.Experiment.route, false) -> Unit
                            else -> navController.navigate(AppDestination.ManageIdentitiesSetup.route) {
                                popUpTo(AppDestination.CreateExperiment.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
        composable(AppDestination.ManageIdentitiesSetup.route) {
            val viewModel: ManageIdentitiesViewModel =
                viewModel(factory = ManageIdentitiesViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ManageIdentitiesScreen(
                uiState = uiState,
                onAddIdentity = {
                    navController.navigate(AppDestination.PresetSelection.route)
                },
                onEditIdentity = { identityId ->
                    navController.navigate(AppDestination.CreateIdentity.route(identityId = identityId))
                },
                onDeleteIdentity = viewModel::deleteIdentity,
                onClearError = viewModel::clearError,
                onContinue = {
                    navController.navigate(AppDestination.Main.route) {
                        popUpTo(AppDestination.Welcome.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        navigation(
            route = AppDestination.Main.route,
            startDestination = AppDestination.Today.route
        ) {
            composable(AppDestination.Today.route) {
                val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(appContainer))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TodayScreen(
                    uiState = uiState,
                    onOpenEditor = viewModel::openEditor,
                    onCloseEditor = viewModel::closeEditor,
                    onEffortChanged = viewModel::updateEffortMinutes,
                    onStatusChanged = viewModel::updateStatus,
                    onEnergyChanged = viewModel::updateEnergy,
                    onMoodChanged = viewModel::updateMood,
                    onResistanceChanged = viewModel::updateResistance,
                    onReflectionChanged = viewModel::updateReflection,
                    onSave = viewModel::saveLog,
                    onOpenIdentity = { identityId ->
                        navController.navigate(AppDestination.IdentityDetail.route(identityId))
                    },
                    onManageIdentities = {
                        navController.navigateToTopLevel(AppDestination.Experiment)
                    }
                )
            }
            composable(AppDestination.Analytics.route) {
                val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.factory(appContainer))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                AnalyticsScreen(
                    uiState = uiState,
                    onOpenIdentity = { identityId ->
                        navController.navigate(AppDestination.IdentityDetail.route(identityId))
                    }
                )
            }
            composable(AppDestination.Experiment.route) {
                val viewModel: ManageIdentitiesViewModel =
                    viewModel(factory = ManageIdentitiesViewModel.factory(appContainer))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ManageIdentitiesScreen(
                    uiState = uiState,
                    onAddIdentity = {
                        navController.navigate(AppDestination.PresetSelection.route)
                    },
                    onEditIdentity = { identityId ->
                        navController.navigate(AppDestination.CreateIdentity.route(identityId = identityId))
                    },
                    onDeleteIdentity = viewModel::deleteIdentity,
                    onClearError = viewModel::clearError,
                    onContinue = null,
                    title = "Experiment",
                    subtitle = "Review the experiment context and manage the identities that power the daily loop."
                )
            }
        }
        composable(
            route = AppDestination.IdentityDetail.route,
            arguments = listOf(
                navArgument("identityId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val identityId = backStackEntry.arguments?.getLong("identityId") ?: 0L
            val viewModel: IdentityDetailViewModel =
                viewModel(factory = IdentityDetailViewModel.factory(appContainer, identityId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            IdentityDetailScreen(uiState = uiState)
        }
    }
}

private fun NavDestination?.isTopLevelDestination(): Boolean {
    val topLevelRoutes = AppDestination.topLevelDestinations.map { it.route }.toSet()
    return this?.hierarchy?.any { destination -> destination.route in topLevelRoutes } == true
}

private fun NavHostController.navigateToTopLevel(destination: AppDestination) {
    val mainGraph = graph.findNode(AppDestination.Main.route) as? NavGraph
    val popUpToId = mainGraph?.findStartDestination()?.id ?: graph.findStartDestination().id
    navigate(destination.route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(popUpToId) {
            saveState = true
        }
    }
}
