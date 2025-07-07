package com.example.tfm.ui.theme.metronome

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MetronomeViewModel : ViewModel() {

    var bpm by mutableStateOf(60)
        private set

    var isRunning by mutableStateOf(false)
        private set

    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private var job: Job? = null

    /* Aumentar / disminuir */
    fun inc() { bpm = (bpm + 1).coerceIn(30, 240) }
    fun dec() { bpm = (bpm - 1).coerceIn(30, 240) }

    /* Play/Stop */
    fun toggle() = if (isRunning) stop() else start()

    private fun start() {
        if (isRunning) return
        isRunning = true
        job = viewModelScope.launch {
            while (isRunning) {
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                delay((60_000 / bpm.toFloat()).roundToInt().toLong())
            }
        }
    }

    private fun stop() {
        isRunning = false
        job?.cancel()
    }

    override fun onCleared() {
        tone.release()
        super.onCleared()
    }
}
