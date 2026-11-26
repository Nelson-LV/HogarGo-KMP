package com.hogargo.kmp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hogargo.kmp.calendar.CalendarViewModel
import com.hogargo.kmp.calendar.ui.CalendarScreen
import com.hogargo.kmp.di.AppModules
import com.hogargo.kmp.finance.FinanceViewModel
import com.hogargo.kmp.home.HomeScreen
import com.hogargo.kmp.home.HomeViewModel
import com.hogargo.kmp.finance.ui.FinanceScreen
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.pet.PetViewModel
import com.hogargo.kmp.pet.ui.PetScreen
import com.hogargo.kmp.household.ui.ChangeNameScreen
import com.hogargo.kmp.household.ui.ChangeRecoveryUserScreen
import com.hogargo.kmp.household.ui.CreateHouseholdScreen
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.household.ui.JoinHouseholdScreen
import com.hogargo.kmp.household.ui.LoginScreen
import com.hogargo.kmp.household.ui.MemberAvatar
import com.hogargo.kmp.household.ui.ProfileScreen
import com.hogargo.kmp.household.ui.RecoverCodesScreen
import com.hogargo.kmp.household.ui.RenameHouseholdScreen
import com.hogargo.kmp.household.ui.WelcomeScreen
import com.hogargo.kmp.tasks.ui.TaskViewModel
import com.hogargo.kmp.tasks.ui.TasksScreen
import com.hogargo.kmp.ui.screens.AboutScreen

/** Rutas de la app. La navegacion vive 100% en commonMain. */
object Routes {
    // Sin sesion
    const val WELCOME = "welcome"
    const val CREATE_HOUSEHOLD = "create-household"
    const val JOIN_HOUSEHOLD = "join-household"
    const val LOGIN = "login"
    const val RECOVER_CODES = "recover-codes"

    // Con sesion
    const val HOME = "home"
    const val TASKS = "tasks"
    const val FINANCE = "finance"
    const val PETS = "pets"
    const val CALENDAR = "calendar"
    const val ABOUT = "about"
    const val PROFILE = "profile"
    const val CHANGE_NAME = "profile/name"
    const val CHANGE_RECOVERY = "profile/recovery"
    const val RENAME_HOUSEHOLD = "profile/household-name"
}

private data class TopLevel(val route: String, val labelEs: String, val labelEn: String, val emoji: String)

private val topLevelDestinations = listOf(
    TopLevel(Routes.TASKS, "Tareas", "Tasks", "⩖"),
    TopLevel(Routes.FINANCE, "Finanzas", "Finances", "💰"),
    TopLevel(Routes.HOME, "Inicio", "Home", "🏠"),
    TopLevel(Routes.PETS, "Mascotas", "Pet", "🐾"),
    TopLevel(Routes.CALENDAR, "Calendario", "Calendar", "📅"),
)

/** Bienvenida y hojas de acceso (sin sesion abierta). */
@Composable
fun WelcomeNavHost(viewModel: HouseholdViewModel) {
    val navController = rememberNavController()
    val close: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCreate = { navController.navigate(Routes.CREATE_HOUSEHOLD) },
                onJoin = { navController.navigate(Routes.JOIN_HOUSEHOLD) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.CREATE_HOUSEHOLD) { CreateHouseholdScreen(viewModel, close) }
        composable(Routes.JOIN_HOUSEHOLD) { JoinHouseholdScreen(viewModel, close) }
        composable(Routes.LOGIN) {
            LoginScreen(viewModel, close, onForgotCode = {
                navController.navigate(Routes.RECOVER_CODES) { popUpTo(Routes.LOGIN) { inclusive = true } }
            })
        }
        composable(Routes.RECOVER_CODES) { RecoverCodesScreen(viewModel, close) }
    }
}

/** App con sesion abierta: pestanas, Acerca de y Perfil. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    modules: AppModules,
    householdViewModel: HouseholdViewModel,
    householdId: String,
    taskViewModel: TaskViewModel = viewModel { TaskViewModel(modules.task, householdViewModel) },
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = topLevelDestinations.any { it.route == currentRoute }
    val household by householdViewModel.household.collectAsState()
    val me by householdViewModel.me.collectAsState()

    Scaffold(
        topBar = {
            if (isTopLevel) {
                if (currentRoute == Routes.HOME) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "HogarGo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = HogarGoColors.primaryText,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                                MemberAvatar(me?.name.orEmpty().ifEmpty { "J" }, 36.dp)
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Routes.PETS) }) {
                                Text("🎁", fontSize = 20.sp)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = HogarGoColors.background),
                    )
                } else {
                    TopAppBar(
                        title = { Text(household?.name.orEmpty(), fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText) },
                        actions = {
                            IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                                MemberAvatar(me?.name.orEmpty(), 36.dp)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = HogarGoColors.background),
                    )
                }
            }
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = { navController.navigateTopLevel(dest.route) },
                            icon = { Text(dest.emoji) },
                            label = { Text(L10n.tr(dest.labelEs, dest.labelEn)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // Las pantallas internas (Perfil) traen su propia barra: no repetir el espacio del sistema.
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel(key = "home-$householdId") {
                        HomeViewModel(modules.finance, modules.calendar, modules.pet, householdId)
                    },
                    householdViewModel = householdViewModel,
                    taskViewModel = taskViewModel,
                    onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                    onNavigateToFinance = { navController.navigate(Routes.FINANCE) },
                    onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                    onNavigateToPet = { navController.navigate(Routes.PETS) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(taskViewModel, householdViewModel)
            }
            composable(Routes.FINANCE) {
                FinanceScreen(viewModel(key = "finance-$householdId") { FinanceViewModel(modules.finance, householdId) })
            }
            composable(Routes.PETS) {
                PetScreen(viewModel(key = "pet-$householdId") { PetViewModel(modules.pet, householdId) })
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(viewModel(key = "calendar-$householdId") { CalendarViewModel(modules.calendar, householdId) })
            }
            composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    householdViewModel,
                    onClose = { navController.popBackStack() },
                    onEditName = { navController.navigate(Routes.CHANGE_NAME) },
                    onEditRecovery = { navController.navigate(Routes.CHANGE_RECOVERY) },
                    onEditHousehold = { navController.navigate(Routes.RENAME_HOUSEHOLD) },
                )
            }
            composable(Routes.CHANGE_NAME) { ChangeNameScreen(householdViewModel) { navController.popBackStack() } }
            composable(Routes.CHANGE_RECOVERY) { ChangeRecoveryUserScreen(householdViewModel) { navController.popBackStack() } }
            composable(Routes.RENAME_HOUSEHOLD) { RenameHouseholdScreen(householdViewModel) { navController.popBackStack() } }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** Fondo vacio mientras se restaura la sesion (para no parpadear). */
@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize())
}
