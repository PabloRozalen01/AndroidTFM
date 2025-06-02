package com.example.tfm.ui.theme.routine

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfm.ui.theme.library.PdfItem
import com.example.tfm.ui.theme.routineDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class RutinaVm(app: Application) : AndroidViewModel(app) {

    private val store = app.routineDataStore
    private val auth  = FirebaseAuth.getInstance()
    private val db    = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(RoutineState())
    val state: StateFlow<RoutineState> = _state

    private val keyDate         = stringPreferencesKey("date")
    private val keyExercises    = stringPreferencesKey("exercises") // JSON array
    private val keyProgress     = intPreferencesKey("progress")     // 0..3

    init {                                         // se llama una vez al día
        viewModelScope.launch {
            store.data.first().let { prefs ->
                val today = LocalDate.now().toString()
                if (prefs[keyDate] == today) {
                    // rutina ya generada hoy
                    _state.value = RoutineState(
                        list = Gson().fromJson(
                            prefs[keyExercises] ?: "[]",
                            object : TypeToken<List<PdfRef>>() {}.type
                        ),
                        progress = prefs[keyProgress] ?: 0,
                        finished = (prefs[keyProgress] ?: 0) == 3
                    )
                } else {
                    // generar nueva
                    generateNewRoutine(today)
                }
            }
        }
    }

    private suspend fun generateNewRoutine(today: String) {
        // 1· leer biblioteca.json
        val json = getApplication<Application>().assets
            .open("biblioteca.json").bufferedReader().readText()
        val map: Map<String, List<PdfItem>> =
            Gson().fromJson(json, object : TypeToken<Map<String, List<PdfItem>>>() {}.type)

        // 2· elegir 3 categorías distintas
        val categories = map.keys.shuffled().take(3)
        val routine = categories.map { cat ->
            val item = map[cat]!!.random()
            PdfRef(cat, item.title, item.file)
        }

        // 3· guardar en DataStore
        store.edit { prefs ->
            prefs[keyDate]      = today
            prefs[keyExercises] = Gson().toJson(routine)
            prefs[keyProgress]  = 0
        }
        _state.value = RoutineState(list = routine)
    }

    fun markDone(index: Int) {
        _state.value = _state.value.copy(progress = index + 1)
        viewModelScope.launch {
            store.edit { it[keyProgress] = index + 1 }
        }
        if (index == 2) finishRoutine()
    }

    private fun finishRoutine() {
        _state.value = _state.value.copy(finished = true)
        viewModelScope.launch {
            store.edit { }                    // progress ya = 3
            // actualizar puntos/racha
            val uid = auth.currentUser?.uid ?: return@launch
            db.collection("users")
                .document(uid)
                .update(
                    mapOf(
                        "points" to com.google.firebase.firestore.FieldValue.increment(5),
                        "streak" to com.google.firebase.firestore.FieldValue.increment(1)
                    )
                )
        }
    }
}

/* -------- datos auxiliares -------- */
data class PdfRef(val category: String, val title: String, val url: String)
data class RoutineState(
    val list: List<PdfRef> = emptyList(),
    val progress: Int = 0,            // 0: ninguno, 1: ejercicio1 hecho…
    val finished: Boolean = false
)
