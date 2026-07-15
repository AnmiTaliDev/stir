package dev.anmitali.stir.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.anmitali.stir.ui.about.AboutScreen
import dev.anmitali.stir.ui.alarmedit.AlarmEditScreen
import dev.anmitali.stir.ui.alarmedit.AlarmEditViewModel
import dev.anmitali.stir.ui.alarmlist.AlarmListScreen
import dev.anmitali.stir.ui.common.localStirApp
import dev.anmitali.stir.ui.groups.GroupEditScreen
import dev.anmitali.stir.ui.groups.GroupEditViewModel
import dev.anmitali.stir.ui.groups.GroupListScreen
import dev.anmitali.stir.ui.onboarding.OnboardingScreen
import dev.anmitali.stir.ui.settings.SettingsScreen
import dev.anmitali.stir.ui.theme.StirTheme
import kotlinx.coroutines.launch

private object Routes {
    const val ONBOARDING = "onboarding"
    const val ALARM_LIST = "alarm_list"
    const val ALARM_EDIT = "alarm_edit/{alarmId}"
    const val GROUPS = "groups"
    const val GROUP_EDIT = "group_edit/{groupId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun alarmEdit(alarmId: Long) = "alarm_edit/$alarmId"
    fun groupEdit(groupId: Long) = "group_edit/$groupId"
}

@Composable
fun StirApp() {
    val app = localStirApp()
    val settings by app.settingsRepository.settings.collectAsState(initial = null)
    val current = settings ?: return

    StirTheme(
        themeMode = current.themeMode,
        dynamicColor = current.dynamicColorEnabled,
    ) {
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()

        NavHost(
            navController = navController,
            startDestination = if (current.onboardingCompleted) Routes.ALARM_LIST else Routes.ONBOARDING,
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        scope.launch { app.settingsRepository.setOnboardingCompleted(true) }
                        navController.navigate(Routes.ALARM_LIST) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.ALARM_LIST) {
                AlarmListScreen(
                    onAddAlarm = { navController.navigate(Routes.alarmEdit(AlarmEditViewModel.NEW_ALARM_ID)) },
                    onEditAlarm = { id -> navController.navigate(Routes.alarmEdit(id)) },
                    onOpenGroups = { navController.navigate(Routes.GROUPS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(
                Routes.ALARM_EDIT,
                arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: AlarmEditViewModel.NEW_ALARM_ID
                AlarmEditScreen(alarmId = alarmId, onDone = { navController.popBackStack() })
            }
            composable(Routes.GROUPS) {
                GroupListScreen(
                    onBack = { navController.popBackStack() },
                    onAddGroup = { navController.navigate(Routes.groupEdit(GroupEditViewModel.NEW_GROUP_ID)) },
                    onEditGroup = { id -> navController.navigate(Routes.groupEdit(id)) },
                )
            }
            composable(
                Routes.GROUP_EDIT,
                arguments = listOf(navArgument("groupId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: GroupEditViewModel.NEW_GROUP_ID
                GroupEditScreen(groupId = groupId, onDone = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
