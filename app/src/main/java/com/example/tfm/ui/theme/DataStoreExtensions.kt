package com.example.tfm.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.routineDataStore: androidx.datastore.core.DataStore<Preferences>
        by preferencesDataStore(name = "routine")