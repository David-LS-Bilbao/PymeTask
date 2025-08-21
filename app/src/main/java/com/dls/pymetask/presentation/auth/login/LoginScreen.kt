package com.dls.pymetask.presentation.auth.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dls.pymetask.R
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.PymeTaskTheme
import com.dls.pymetask.ui.theme.Roboto
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // --- Estado de VM ---
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val googleIntent by viewModel.googleSignInIntent.collectAsState()

    // --- Estado local ---
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Launcher para Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        result.data?.let { data ->
            viewModel.onGoogleSignInResult(context, data)
        }
    }

    // Navega cuando el login termina bien
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) onLoginSuccess()
    }
    // Lanza flujo de Google cuando el intent está listo
    LaunchedEffect(googleIntent) {
        googleIntent?.let { launcher.launch(IntentSenderRequest.Builder(it).build()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        // Logo (decorativo -> contentDescription = null)
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        // Título localizado
        Text(
            text = stringResource(R.string.login_welcome_title),
            style = TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        )

        // Subtítulo localizado
        Text(
            text = stringResource(R.string.login_welcome_subtitle),
            style = TextStyle(
                fontFamily = Roboto,
                fontSize = 16.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )
        )

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null // decorativo
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        // Mensaje de error (si el VM emite cadenas localizadas, saldrán ya traducidas)
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Botón "Iniciar sesión"
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    // Envía el error ya localizado al VM (si tu VM muestra este mismo campo)
                    scope.launch { viewModel.setError(context.getString(R.string.auth_fill_all)) }
                } else {
                    viewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.login_sign_in))
        }

        // Botón "Iniciar sesión con Google"
        Button(
            onClick = { viewModel.launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.login_sign_in_google))
        }

        // Pie: "¿No tienes cuenta? Registrarse"
        Row {
            Text(stringResource(R.string.login_no_account))
            Text(
                text = stringResource(R.string.auth_register),
                color = Color(0xFF1976D2),
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    PymeTaskTheme {
        // Preview simple usando strings localizados
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Text(
                stringResource(R.string.login_welcome_title),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            )
            Text(
                stringResource(R.string.login_welcome_subtitle),
                fontFamily = Roboto,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.auth_email)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.auth_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.login_sign_in))
            }

            Row {
                Text(stringResource(R.string.login_no_account))
                Text(
                    text = stringResource(R.string.auth_register),
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

