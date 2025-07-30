package com.dls.pymetask.presentation.auth.login



import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val googleIntent by viewModel.googleSignInIntent.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val data = result.data
       // if (data != null) viewModel.onGoogleSignInResult(data)
        if (data != null) viewModel.onGoogleSignInResult(context, data)

    }


    // Lanza navegación cuando el login es exitoso
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) onLoginSuccess()
    }
    LaunchedEffect(googleIntent) {
        googleIntent?.let {
            launcher.launch(IntentSenderRequest.Builder(it).build())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        // Título
        Text(
            text = "Bienvenido a PymeTask",
            style = TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        )

        // Subtítulo
        Text(
            text = "Gestiona tu negocio de forma simple y profesional",
            style = TextStyle(
                fontFamily = Roboto,
                fontSize = 16.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        // Error
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Botón de inicio de sesión
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    scope.launch { viewModel.setError("Rellena todos los campos") }
                } else {
                    viewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar sesión")
        }

        // Botón de inicio de sesión con Google
        Button(
            onClick = { viewModel.launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar sesión con Google")
        }

        // Registro
        Row {
            Text("¿No tienes cuenta? ")
            Text(
                text = "Registrarse",
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
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Text("Bienvenido a PymeTask", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            Text("Gestiona tu negocio de forma simple y profesional", fontFamily = Roboto, fontSize = 16.sp)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }



            Row {
                Text("¿No tienes cuenta? ")
                Text(
                    text = "Registrarse",
                color = Color(0xFF1976D2),
                modifier = Modifier.clickable { }
                )
            }
        }
    }
}


