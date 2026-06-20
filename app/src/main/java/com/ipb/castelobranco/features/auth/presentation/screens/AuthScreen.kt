// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/auth/AuthView.kt
package com.ipb.castelobranco.features.auth.presentation.screens

import timber.log.Timber
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.ColorFilter
import com.ipb.castelobranco.core.presentation.theme.primaryLight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.ipb.castelobranco.BuildConfig
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.features.auth.presentation.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.credentials.CustomCredential

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    val isGoogleLoading by viewModel.isGoogleLoading.collectAsState()
    var isCredentialPending by remember { mutableStateOf(false) }
    val isGoogleBusy = isCredentialPending || isGoogleLoading
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(loginError) {
        if (!loginError.isNullOrBlank()) {
            delay(5000)
            viewModel.clearLoginError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthViewModel.AuthEvent.LoginSuccess -> onAuthSuccess()

                else -> {}
            }
        }
    }

    suspend fun launchGoogleSignIn() {
        Timber.d("Starting Google Sign-In flow")
        isCredentialPending = true
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            Timber.d("Calling credentialManager.getCredential...")
            val response = credentialManager.getCredential(context, request)
            val credential = response.credential
            Timber.d("Credential received: %s - type: %s", credential::class.simpleName, credential.type)

            if (credential is GoogleIdTokenCredential) {
                isCredentialPending = false
                viewModel.signInWithGoogle(credential.idToken)
            } else if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                if (BuildConfig.DEBUG) Timber.d("idToken extracted: %s", googleCredential.idToken.take(20))
                isCredentialPending = false
                viewModel.signInWithGoogle(googleCredential.idToken)
            } else {
                Timber.w("Unrecognized credential type: %s", credential.type)
                isCredentialPending = false
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            isCredentialPending = false
        }
    }

    BaseScreen(
        tabName = "Autenticação",
        logoRes = R.drawable.ic_auth_login,
        showBackArrow = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Image(
                painter = painterResource(id = R.drawable.ic_sarca_ipb),
                contentDescription = "Logo SARCA IPB",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(primaryLight),
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Text(
                text = "Castelo Branco",
                style = MaterialTheme.typography.titleLarge,
                color = primaryLight,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    val img =
                        if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = img,
                        contentDescription = if (passwordVisible.value) "Ocultar senha" else "Mostrar senha",
                        modifier = Modifier.clickable {
                            passwordVisible.value = !passwordVisible.value
                        }
                    )
                }
            )

            Button(
                onClick = { viewModel.singIn(username.value, password.value) },
                shape = RoundedCornerShape(8.dp),
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


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "ou",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = { if (!isGoogleBusy) coroutineScope.launch { launchGoogleSignIn() } },
                enabled = !isGoogleBusy,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (isGoogleBusy) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conectando...")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar com Google")
                    }
                }
            }


            Spacer(modifier = Modifier.weight(1f))
        }
    }
}