package com.example.tfm.ui.theme

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tfm.ui.theme.routine.PdfRef
import com.example.tfm.ui.theme.routine.RutinaVm
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate



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
@RequiresApi(Build.VERSION_CODES.O)      // por LocalDate
@Composable
fun HomeScreen(nav: NavHostController) {

    val ctx = LocalContext.current
    val routineDone = routineDoneToday(ctx)   // ← comprobar DataStore

    Scaffold { p ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            /* 1 · Perfil */
            HomeBtn("Perfil",
                onClick = { nav.navigate(Screen.Profile.route) },
                align = Alignment.TopStart)

            /* 2 · Social */
            HomeBtn("Social",
                onClick = { nav.navigate(Screen.Social.route) },
                align = Alignment.TopEnd)

            /* 3 · Metrónomo */
            HomeBtn("Metrónomo",
                onClick = { nav.navigate(Screen.Metronome.route) },
                align = Alignment.BottomStart)

            /* 4 · Biblioteca */
            HomeBtn("Biblioteca",
                onClick = { nav.navigate(Screen.Library.route) },
                align = Alignment.BottomEnd)

            /* 5 · Rutina (centro) */
            HomeBtn(
                text = "Rutina",
                onClick = { nav.navigate(Screen.Routine.route) },
                align = Alignment.Center,
                enabled = !routineDone
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

/* ───────── Perfil ───────── */
data class ProfileState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val points: Int = 0,
    val streak: Int = 0,
    val loading: Boolean = true
)

class ProfileVm : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init {
        val user = auth.currentUser

        if (user == null) {
            // no hay sesión → quedamos en loading = false
            _state.value = _state.value.copy(loading = false)
        } else {
            // datos básicos que ya vienen de FirebaseAuth
            _state.value = _state.value.copy(
                displayName = user.displayName ?: user.email ?: "",
                email       = user.email ?: "",
                photoUrl    = user.photoUrl?.toString()
            )

            // puntos y racha desde Firestore
            viewModelScope.launch {
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        _state.value = _state.value.copy(
                            displayName = doc.getString("displayName")
                                ?: _state.value.displayName,
                            photoUrl = doc.getString("photoUrl")
                                ?: _state.value.photoUrl,
                            points  = doc.getLong("points")?.toInt() ?: 0,
                            streak  = doc.getLong("streak")?.toInt() ?: 0,
                            loading = false
                        )
                    }
                    .addOnFailureListener {
                        _state.value = _state.value.copy(loading = false)
                    }
            }
        }
    }


    fun logout() = auth.signOut()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavHostController) {
    val vm: ProfileVm = viewModel()
    val ui by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Screen.Home.route) { popUpTo(0) }
                    }) { Icon(Icons.Default.ArrowBack, "Inicio") }
                }
            )
        }
    ) { p ->
        if (ui.loading) {
            Box(Modifier.fillMaxSize().padding(p), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto (si la hay)
                ui.photoUrl?.let {
                    AsyncImage(                     // coil-compose
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Text(ui.displayName, style = MaterialTheme.typography.titleLarge)
                Text(ui.email, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(24.dp))

                Row {
                    StatBox("Racha", ui.streak.toString())
                    Spacer(Modifier.width(16.dp))
                    StatBox("Puntos", ui.points.toString())
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        vm.logout()
                        nav.navigate(Screen.Welcome.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cerrar sesión") }
            }
        }
    }
}

/* ───────── Rutina ───────── */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineScreen(nav: NavHostController) {
    val vm: RutinaVm = viewModel()
    val ui by vm.state.collectAsState()

    if (ui.finished) {
        // Pantalla de enhorabuena
        Scaffold { p ->
            Column(
                Modifier.fillMaxSize().padding(p),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("¡Rutina completada!", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { nav.navigate(Screen.Home.route) { popUpTo(0) } }) {
                    Text("Volver a Inicio")
                }
            }
        }
        return
    }

    var currentPdf by remember { mutableStateOf<PdfRef?>(null) }

    currentPdf?.let { pdf ->
        Dialog(onDismissRequest = { currentPdf = null; vm.markDone(ui.progress) }) {
            Surface {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(
                                "https://drive.google.com/viewerng/viewer?embedded=true&url=${Uri.encode(pdf.url)}"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Rutina diaria") },
            navigationIcon = {
                IconButton(onClick = { nav.navigate(Screen.Home.route) { popUpTo(0) } }) {
                    Icon(Icons.Default.ArrowBack, "Inicio")
                }
            }
        )
    }) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            listOf(0, 1, 2).forEach { idx ->
                Button(
                    onClick = { currentPdf = ui.list[idx] },
                    enabled = idx <= ui.progress,                 // secuencial
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ejercicio ${idx + 1}") }
            }
        }
    }
}

@Composable
private fun RowScope.StatBox(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BoxScope.HomeBtn(
    text: String,
    onClick: () -> Unit,
    align: Alignment,
    enabled: Boolean = true
) {
    Button(
        enabled=enabled,
        onClick = onClick,
        modifier = Modifier
            .align(align)
            .padding(16.dp)
    ) {
        Text(text)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun routineDoneToday(ctx: Context): Boolean {
    val keyDate     = stringPreferencesKey("date")
    val keyProgress = intPreferencesKey("progress")
    val today       = LocalDate.now().toString()

    // collectAsState para recomponerse si cambia
    val state by ctx.routineDataStore.data
        .map { prefs ->
            prefs[keyDate] == today && prefs[keyProgress] == 3
        }
        .collectAsState(initial = false)

    return state
}

/* ─────────────  PLACEHOLDERS  ───────────── */
/* Usa esta plantilla para todas las pantallas secundarias */

@Composable
fun MetronomeScreen(nav: NavHostController)  = ScreenWithHome("Metrónomo", nav)
@Composable
fun TunerScreen(nav: NavHostController)      = ScreenWithHome("Afinador", nav)
@Composable
fun SocialScreen(nav: NavHostController)     = ScreenWithHome("Social", nav)

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
