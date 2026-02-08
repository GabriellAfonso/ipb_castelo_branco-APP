// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/auth/AuthView.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.main.goToMainAsRoot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthView(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()

    LaunchedEffect(loginError) {
        if (!loginError.isNullOrBlank()) {
            delay(5000)
            viewModel.clearLoginError()
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthViewModel.AuthEvent.LoginSuccess -> {
                    val msg = "Logado com Sucesso"
                    (context as? Activity)?.goToMainAsRoot(message = msg)
                }
                else -> {}
            }
        }
    }

    BaseScreen(
        tabName = "Autenticação",
        logoRes = R.drawable.ic_auth_login,
        showBackArrow = true,
        onBackClick = onBackClick,
        showAccountAction = false
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!loginError.isNullOrBlank()) {
                Text(
                    text = loginError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Nome de Usuário") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Senha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val img = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = img,
                        contentDescription = if (passwordVisible.value) "Ocultar senha" else "Mostrar senha",
                        modifier = Modifier.clickable { passwordVisible.value = !passwordVisible.value }
                    )
                }
            )

            Button(
                onClick = { viewModel.singIn(username.value, password.value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Entrar")
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Ainda não tem uma conta? Registre-se")
            }

//            Button(
//                onClick = { viewModel.signInWithGoogle() },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 12.dp)
//            ) {
//                Text("Entrar com Google")
//            }
        }
    }
}
