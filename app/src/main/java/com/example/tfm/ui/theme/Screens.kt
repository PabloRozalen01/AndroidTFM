package com.example.tfm.ui.theme

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.tfm.ui.theme.metronome.MetronomeViewModel
import com.example.tfm.ui.theme.routine.PdfRef
import com.example.tfm.ui.theme.routine.RutinaVm
import com.example.tfm.ui.theme.social.Friend
import com.example.tfm.ui.theme.social.SocialVm
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import com.example.tfm.R
import kotlinx.coroutines.delay


/* --------- WELCOME --------- */
@Composable
fun WelcomeScreen(nav: NavHostController) {

    Scaffold { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_oboeapp_text),
                contentDescription = "Logo OboeApp",
                modifier = Modifier
                    .height(250.dp)
                    .padding(bottom = 48.dp)
            )

            Button(
                onClick = { nav.navigate(Screen.Login.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
        }
    }
}
/* --------- HOME --------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(nav: NavHostController) {

    val ctx = LocalContext.current
    val routineDone = routineDoneToday(ctx)

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

class BibliotecaVm(application: Application) : AndroidViewModel(application) {
    var data: Map<String, List<PdfItem>>
            by mutableStateOf(emptyMap())
        private set

    init {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                application.assets.open("biblioteca.json")
                    .bufferedReader()
                    .readText()
            }

            val type = object : TypeToken<Map<String, List<PdfItem>>>() {}.type

            data = Gson().fromJson(json, type)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavHostController) {

    val vm: BibliotecaVm = viewModel()
    val ctx = LocalContext.current
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
            _state.value = _state.value.copy(loading = false)
        } else {
            _state.value = _state.value.copy(
                displayName = user.displayName ?: user.email ?: "",
                email       = user.email ?: "",
                photoUrl    = user.photoUrl?.toString()
            )


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
                ui.photoUrl?.let {
                    AsyncImage(
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
                    enabled = idx <= ui.progress,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(nav: NavHostController) {

    val vm: MetronomeViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metrónomo") },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Screen.Home.route) { popUpTo(0) }
                    }) { Icon(Icons.Default.ArrowBack, "Inicio") }
                }
            )
        }
    ) { p ->
        MetronomePane(vm, Modifier.padding(p))
    }
}

/* ---------- UI del metrónomo ---------- */
@Composable
fun MetronomePane(
    vm: MetronomeViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = vm::dec) { Icon(Icons.Default.Remove, null) }
            Text("${vm.bpm}", style = MaterialTheme.typography.displayLarge)
            IconButton(onClick = vm::inc) { Icon(Icons.Default.Add, null) }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = vm::toggle) {
            Text(if (vm.isRunning) "Parar" else "Iniciar")
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(nav: NavHostController) {

    /* ---------- estado ---------- */
    val vm: SocialVm = viewModel()
    val list by vm.friends.collectAsState()

    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: "—"
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    var uidToAdd by remember { mutableStateOf("") }
    var adding   by remember { mutableStateOf(false) }

    /* ---------- UI ---------- */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social") },
                navigationIcon = {
                    IconButton(
                        onClick = { nav.navigate(Screen.Home.route) { popUpTo(0) } }
                    ) { Icon(Icons.Default.ArrowBack, contentDescription = "Inicio") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Añadir amigo") },
                icon = { Icon(Icons.Default.PersonAdd, null) },
                onClick = { adding = true }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Tu UID", style = MaterialTheme.typography.labelMedium)
                        Text(myUid, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(myUid))
                        copied = true
                    }) { Icon(Icons.Default.ContentCopy, "Copiar UID") }
                }
            }

            if (copied) {
                LaunchedEffect(Unit) {
                    delay(1500)
                    copied = false
                }
                Text(
                    "UID copiado",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }

            Text(
                "Amigos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(list, key = { it.uid }) { friend -> FriendRow(friend) }
            }
        }


        if (adding) {
            AlertDialog(
                onDismissRequest = { adding = false },
                confirmButton = {
                    TextButton(onClick = {
                        vm.addFriendByUid(uidToAdd.trim()) { adding = false; uidToAdd = "" }
                    }) { Text("Añadir") }
                },
                dismissButton = { TextButton(onClick = { adding = false }) { Text("Cancelar") } },
                title = { Text("UID del amigo") },
                text  = {
                    OutlinedTextField(
                        value = uidToAdd,
                        onValueChange = { uidToAdd = it },
                        singleLine = true,
                        label = { Text("UID") }
                    )
                }
            )
        }
    }
}

@Composable
private fun FriendRow(f: Friend) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(     // coil-compose
                model = f.photo ?: R.drawable.ic_user_placeholder,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(f.name, style = MaterialTheme.typography.titleMedium)
                Text("${f.streak} días de racha", style = MaterialTheme.typography.bodySmall)
            }
            Text("${f.points} pts", style = MaterialTheme.typography.bodyMedium)
        }
    }
}