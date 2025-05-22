package com.example.tfm.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfm.auth.AuthViewModel
import com.example.tfm.nav.Screen

/* --------- WELCOME --------- */
@Composable
fun WelcomeScreen(nav: NavHostController, authVm: AuthViewModel) {
    Scaffold { p ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(p),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                if (authVm.currentUser == null)
                    nav.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                else
                    nav.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
            }) {
                Text("Entrar")
            }
        }
    }
}
/* --------- HOME --------- */
@Composable
fun HomeScreen(nav: NavHostController) {
    Scaffold { p ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            /* 1 · Perfil Arriba-Izquierda */
            HomeBtn(
                text = "Perfil",
                onClick = { nav.navigate(Screen.Profile.route) },
                align = Alignment.TopStart
            )

            /* 2 · Social Arriba-Derecha */
            HomeBtn(
                text = "Social",
                onClick = { nav.navigate(Screen.Social.route) },
                align = Alignment.TopEnd
            )

            /* 3 · Metronomo Abajo-Izquierda */
            HomeBtn(
                text = "Metrónomo",
                onClick = { nav.navigate(Screen.Metronome.route) },
                align = Alignment.BottomStart
            )

            /* 4 · Biblioteca Abajo-Derecha */
            HomeBtn(
                text = "Biblioteca",
                onClick = { nav.navigate(Screen.Library.route) },
                align = Alignment.BottomEnd
            )

            /* 5 · Rutina Centro */
            HomeBtn(
                text = "Rutina",
                onClick = { nav.navigate(Screen.Routine.route) },
                align = Alignment.Center
            )
        }
    }
}

@Composable
private fun BoxScope.HomeBtn(
    text: String,
    onClick: () -> Unit,
    align: Alignment
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .align(align)
            .padding(16.dp)
    ) {
        Text(text)
    }
}

/* ─────────────  PLACEHOLDERS  ───────────── */
/* Usa esta plantilla para todas las pantallas secundarias */

@Composable
fun MetronomeScreen(nav: NavHostController)  = ScreenWithHome("Metrónomo", nav)
@Composable
fun TunerScreen(nav: NavHostController)      = ScreenWithHome("Afinador", nav)
@Composable
fun LibraryScreen(nav: NavHostController)    = ScreenWithHome("Biblioteca", nav)
@Composable
fun ProfileScreen(nav: NavHostController)    = ScreenWithHome("Perfil", nav)
@Composable
fun SocialScreen(nav: NavHostController)     = ScreenWithHome("Social", nav)
@Composable
fun RoutineScreen(nav: NavHostController)    = ScreenWithHome("Rutina diaria", nav)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenWithHome(title: String, nav: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Screen.Home.route) { popUpTo(0) }
                    }) { Text("Inicio") }
                }
            )
        }
    ) { p ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(p),
            contentAlignment = Alignment.Center
        ) { Text("Pantalla $title") }
    }
}
