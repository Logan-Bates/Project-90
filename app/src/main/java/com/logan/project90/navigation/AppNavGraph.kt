package com.logan.project90.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.logan.project90.di.AppContainer
import com.logan.project90.ui.experiment.CreateExperimentScreen
import com.logan.project90.ui.experiment.CreateExperimentViewModel
import com.logan.project90.ui.identity.CreateIdentityScreen
import com.logan.project90.ui.identity.CreateIdentityViewModel
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
                        !uiState.hasIdentity -> AppDestination.CreateIdentity.route
                        else -> AppDestination.Today.route
                    }
                    navController.navigate(target)
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
                        navController.navigate(AppDestination.CreateIdentity.route)
                    }
                }
            )
        }
        composable(AppDestination.CreateIdentity.route) {
            val viewModel: CreateIdentityViewModel =
                viewModel(factory = CreateIdentityViewModel.factory(appContainer))
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
                        navController.navigate(AppDestination.Today.route) {
                            popUpTo(AppDestination.Welcome.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(AppDestination.Today.route) {
            val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TodayScreen(
                uiState = uiState,
                onEffortChanged = viewModel::updateEffortMinutes,
                onStatusChanged = viewModel::updateStatus,
                onEnergyChanged = viewModel::updateEnergy,
                onMoodChanged = viewModel::updateMood,
                onResistanceChanged = viewModel::updateResistance,
                onReflectionChanged = viewModel::updateReflection,
                onSave = viewModel::saveLog
            )
        }
    }
}
