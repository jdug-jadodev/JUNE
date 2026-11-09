package com.andromeda.gastos.infrastructure.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.infrastructure.ui.components.SplashScreen
import com.andromeda.gastos.infrastructure.ui.screen.AddExpenseScreen
import com.andromeda.gastos.infrastructure.ui.screen.AnalisisIAScreen
import com.andromeda.gastos.infrastructure.ui.screen.AnalisisScreen
import com.andromeda.gastos.infrastructure.ui.screen.BuscarScreen
import com.andromeda.gastos.infrastructure.ui.screen.ForgotPasswordScreen
import com.andromeda.gastos.infrastructure.ui.screen.HomeScreen
import com.andromeda.gastos.infrastructure.ui.screen.IngresosScreen
import com.andromeda.gastos.infrastructure.ui.screen.LoginScreen
import com.andromeda.gastos.infrastructure.ui.screen.MembersScreen
import com.andromeda.gastos.infrastructure.ui.screen.MonthDetailScreen
import com.andromeda.gastos.infrastructure.ui.screen.MonthsScreen
import com.andromeda.gastos.infrastructure.ui.screen.RegisterScreen
import com.andromeda.gastos.infrastructure.ui.screen.SecurityScreen
import com.andromeda.gastos.infrastructure.ui.screen.SettingsScreen
import com.andromeda.gastos.infrastructure.ui.viewmodel.AppViewModel
import com.andromeda.gastos.infrastructure.ui.viewmodel.AuthState
import com.andromeda.gastos.infrastructure.ui.viewmodel.AuthViewModel
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeViewModel
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthDetailViewModel
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthsViewModel

@Composable
fun AndromedaNavHost(container: AppContainer) {
    val appVm: AppViewModel = viewModel(factory = AppViewModel.factory(container))
    val appState by appVm.state.collectAsStateWithLifecycle()

    when (val estado = appState) {
        AuthState.Cargando -> SplashScreen()
        AuthState.SinSesion -> AuthFlow(container, onSesion = appVm::onSesion)
        is AuthState.ConSesion -> MainFlow(
            container = container,
            sesion = estado.sesion,
            onLogout = appVm::logout,
        )
    }
}

@Composable
private fun AuthFlow(
    container: AppContainer,
    onSesion: (Sesion) -> Unit,
) {
    val nav = rememberNavController()
    val vm: AuthViewModel = viewModel(factory = AuthViewModel.factory(container))

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                vm = vm,
                onIrARegistro = { nav.navigate("register") },
                onIrARecuperar = { nav.navigate("forgot") },
                onSesion = onSesion,
            )
        }
        composable("register") {
            RegisterScreen(
                vm = vm,
                onVolver = { nav.popBackStack() },
                onSesion = onSesion,
            )
        }
        composable("forgot") {
            ForgotPasswordScreen(
                container = container,
                onVolver = { nav.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainFlow(
    container: AppContainer,
    sesion: Sesion,
    onLogout: () -> Unit,
) {
    val nav = rememberNavController()
    val homeVm: HomeViewModel = viewModel(
        key = "home-${sesion.usuario.id}",
        factory = HomeViewModel.factory(container),
    )

    LaunchedEffect(homeVm) {
        homeVm.cargar()
    }

    DisposableEffect(homeVm) {
        onDispose { homeVm.detener() }
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                vm = homeVm,
                onAgregar = { hogarId -> nav.navigate("add/$hogarId?autoVoz=false") },
                onVoz = { hogarId -> nav.navigate("add/$hogarId?autoVoz=true") },
                onMiembros = { hogarId -> nav.navigate("members/$hogarId") },
                onAjustes = { hogarId -> nav.navigate("settings/$hogarId") },
                onAnalisis = { hogarId -> nav.navigate("analisis/$hogarId") },
                onVerMeses = { hogarId -> nav.navigate("months/$hogarId") },
                onBuscar = { hogarId -> nav.navigate("buscar/$hogarId") },
            )
        }
        composable("buscar/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            BuscarScreen(
                container = container,
                hogarId = hogarId,
                onVolver = { nav.popBackStack() },
            )
        }
        composable("analisis/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            AnalisisScreen(
                container = container,
                hogarId = hogarId,
                onVolver = { nav.popBackStack() },
                onAnalizarIA = { inicio, fin ->
                    nav.navigate("analisis-ia/$hogarId?inicio=$inicio&fin=$fin")
                },
            )
        }
        composable(
            route = "analisis-ia/{hogarId}?inicio={inicio}&fin={fin}",
            arguments = listOf(
                navArgument("hogarId") { type = NavType.StringType },
                navArgument("inicio") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("fin") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            AnalisisIAScreen(
                container = container,
                hogarId = hogarId,
                inicio = entrada.arguments?.getString("inicio"),
                fin = entrada.arguments?.getString("fin"),
                onVolver = { nav.popBackStack() },
            )
        }
        composable("months/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            val vm: MonthsViewModel = viewModel(
                key = "months-$hogarId",
                factory = MonthsViewModel.factory(container),
            )
            MonthsScreen(
                vm = vm,
                hogarId = hogarId,
                onAbrirCiclo = { inicio, fin ->
                    nav.navigate("cycle/$hogarId/$inicio/$fin")
                },
                onVolver = { nav.popBackStack() },
            )
        }
        composable(
            route = "cycle/{hogarId}/{inicio}/{fin}",
            arguments = listOf(
                navArgument("hogarId") { type = NavType.StringType },
                navArgument("inicio") { type = NavType.StringType },
                navArgument("fin") { type = NavType.StringType },
            ),
        ) { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            val inicio = entrada.arguments?.getString("inicio").orEmpty()
            val fin = entrada.arguments?.getString("fin").orEmpty()
            val vm: MonthDetailViewModel = viewModel(
                key = "cycle-$hogarId-$inicio-$fin",
                factory = MonthDetailViewModel.factory(container),
            )
            MonthDetailScreen(
                vm = vm,
                hogarId = hogarId,
                inicio = inicio,
                fin = fin,
                onVolver = { nav.popBackStack() },
            )
        }
        composable(
            route = "add/{hogarId}?autoVoz={autoVoz}",
            arguments = listOf(
                navArgument("hogarId") { type = NavType.StringType },
                navArgument("autoVoz") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            val autoVoz = entrada.arguments?.getBoolean("autoVoz") ?: false
            AddExpenseScreen(
                container = container,
                hogarId = hogarId,
                autoVoz = autoVoz,
                onVolver = { nav.popBackStack() },
            )
        }
        composable("members/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            MembersScreen(
                container = container,
                hogarId = hogarId,
                onVolver = { nav.popBackStack() },
            )
        }
        composable("settings/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            SettingsScreen(
                container = container,
                hogarId = hogarId,
                onSeguridad = { nav.navigate("security/$hogarId") },
                onVolver = { nav.popBackStack() },
                onLogout = onLogout,
                onIngresos = { nav.navigate("ingresos/$hogarId") },
            )
        }
        composable("ingresos/{hogarId}") { entrada ->
            val hogarId = entrada.arguments?.getString("hogarId").orEmpty()
            IngresosScreen(
                container = container,
                hogarId = hogarId,
                onVolver = { nav.popBackStack() },
            )
        }
        composable("security/{hogarId}") { entrada ->
            SecurityScreen(
                container = container,
                hogarId = entrada.arguments?.getString("hogarId").orEmpty(),
                onVolver = { nav.popBackStack() },
            )
        }
    }
}
