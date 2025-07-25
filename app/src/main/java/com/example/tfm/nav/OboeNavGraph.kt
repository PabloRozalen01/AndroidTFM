package com.example.tfm.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfm.auth.*
import com.example.tfm.ui.theme.*

sealed class Screen(val route: String) {
    object Welcome    : Screen("welcome")
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Home       : Screen("home")
    object Metronome  : Screen("metronome")
    object Library    : Screen("library")
    object Profile    : Screen("profile")
    object Social     : Screen("social")
    object Routine    : Screen("routine")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OboeNavGraph(
    nav: NavHostController = rememberNavController(),
    authVm: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    NavHost(nav, startDestination = Screen.Welcome.route) {

        composable(Screen.Welcome.route) {
            WelcomeScreen(nav)
        }
        composable(Screen.Login.route)    { LoginScreen(nav, authVm) }
        composable(Screen.Register.route) { RegisterScreen(nav, authVm) }
        composable(Screen.Home.route)     { HomeScreen(nav) }
        composable(Screen.Metronome.route){ MetronomeScreen(nav) }
        composable(Screen.Library.route)  { LibraryScreen(nav) }
        composable(Screen.Profile.route)  { ProfileScreen(nav) }
        composable(Screen.Social.route)   { SocialScreen(nav) }
        composable(Screen.Routine.route)  { RoutineScreen(nav) }
    }
}