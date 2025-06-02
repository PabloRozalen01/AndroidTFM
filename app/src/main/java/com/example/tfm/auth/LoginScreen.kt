package com.example.tfm.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfm.nav.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavHostController, vm: AuthViewModel) {

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var passVisible  by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()

    Scaffold { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation =
                    if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(icon, contentDescription =
                            if (passVisible) "Ocultar contraseña" else "Mostrar contraseña")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.login(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entrar") }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { nav.navigate(Screen.Register.route) }) {
                Text("Crear cuenta")
            }
        }
    }

    WhenSuccess(state) {
        nav.navigate(Screen.Home.route) { popUpTo(0) }
    }
}

@Composable
fun RegisterScreen(nav: NavHostController, vm: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()

    AuthForm(
        title = "Crear cuenta",
        email = email,
        pass = pass,
        onEmail = { email = it },
        onPass  = { pass = it },
        onSubmit = { vm.register(email, pass) },
        onSwitch = { nav.popBackStack() }
    )

    WhenSuccess(state) { nav.navigate(Screen.Home.route) { popUpTo(0) } }
}

@Composable
private fun AuthForm(
    title: String,
    email: String,
    pass: String,
    onEmail: (String) -> Unit,
    onPass:  (String) -> Unit,
    onSubmit: () -> Unit,
    onSwitch: () -> Unit
) {
    Scaffold { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmail,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = onPass,
                label = { Text("Contraseña") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Aceptar") }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSwitch) { Text("Cambiar a ${if (title == "Iniciar sesión") "registro" else "login"}") }
        }
    }
}

@Composable
private fun WhenSuccess(state: AuthState, block: () -> Unit) {
    if (state is AuthState.Success) LaunchedEffect(Unit) { block() }
}