package com.example.tfm.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Success : AuthState
    data class Error(val msg: String) : AuthState
}

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state
    val currentUser get() = repo.currentUser

    fun register(mail: String, pass: String) = authFlow { repo.register(mail, pass) }
    fun login(mail: String, pass: String)    = authFlow { repo.login(mail, pass) }
    fun logout() = repo.logout()

    private fun authFlow(block: suspend () -> Unit) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching { block() }
            .onSuccess { _state.value = AuthState.Success }
            .onFailure { _state.value = AuthState.Error(it.message ?: "Error") }
    }

    companion object {
        /** Fábrica estándar para usar con viewModel(factory = …) */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(AuthRepository()) as T
        }
    }
}