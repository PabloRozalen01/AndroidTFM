package com.example.tfm.ui.theme.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PdfItem(val title: String, val file: String)

class BibliotecaViewModel(app: Application) : AndroidViewModel(app) {

    val data: MutableState<Map<String, List<PdfItem>>> = mutableStateOf(emptyMap())

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() = withContext(Dispatchers.IO) {
        val json = getApplication<Application>()
            .assets.open("biblioteca.json").bufferedReader().use { it.readText() }
        val map: Map<String, List<PdfItem>> =
            Gson().fromJson(json, Map::class.java) as Map<String, List<PdfItem>>
        data.value = map
    }
}
