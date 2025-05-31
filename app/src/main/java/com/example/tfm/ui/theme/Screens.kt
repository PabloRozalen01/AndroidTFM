package com.example.tfm.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfm.auth.AuthViewModel
import com.example.tfm.nav.Screen
import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

/* ───────── Biblioteca ───────── */


data class PdfItem(val title: String, val file: String)

class BibliotecaVm(application: Application) : AndroidViewModel(application) { // <-- público
    var data: Map<String, List<PdfItem>>          //  ⬅️ tipo bien explícito
            by mutableStateOf(emptyMap())
        private set

    init {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                application.assets.open("biblioteca.json")
                    .bufferedReader()
                    .readText()
            }
            // type seguro para Map<String, List<PdfItem>>
            val type = object : TypeToken<Map<String, List<PdfItem>>>() {}.type

            // parseo y asignación
            data = Gson().fromJson(json, type)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavHostController) {

    val vm: BibliotecaVm = viewModel()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    /* permiso de notificaciones (Android 13+) */
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /* visor WebView en diálogo */
    var pdfUrl by remember { mutableStateOf<String?>(null) }
    pdfUrl?.let { url ->
        Dialog(onDismissRequest = { pdfUrl = null }) {
            Surface {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            // Google Docs viewer
                            loadUrl(
                                "https://drive.google.com/viewerng/viewer?embedded=true&url=${Uri.encode(url)}"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca") },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Screen.Home.route) { popUpTo(0) }
                    }) { Icon(Icons.Default.ArrowBack, "Inicio") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            vm.data.forEach { (section, list) ->
                if (list.isNotEmpty()) {
                    item {
                        Text(
                            section.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(list) { pdf ->
                        ListItem(
                            headlineContent = { Text(pdf.title) },

                            modifier = Modifier
                                .clickable { pdfUrl = pdf.file }
                                .padding(horizontal = 16.dp)
                        )
                        Divider()
                    }
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
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
