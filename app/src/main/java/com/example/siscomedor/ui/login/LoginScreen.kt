package com.example.siscomedor.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.CODE_LENGTH
import com.example.siscomedor.domain.checkCredentials
import com.example.siscomedor.domain.sanitizeCode
import com.example.siscomedor.domain.validateCode
import com.example.siscomedor.domain.validatePassword
import com.example.siscomedor.ui.components.AppIcon
import com.example.siscomedor.ui.components.Notice
import com.example.siscomedor.ui.components.PageTitle

// Estado que dibuja el formulario. Solo valores: así LoginContent se puede previsualizar.
data class LoginUiState(
    val code: String = "",
    val password: String = "",
    val remember: Boolean = false,
    val showPassword: Boolean = false,
    val codeTouched: Boolean = false,
    val passwordTouched: Boolean = false,
    val loginError: String? = null
) {
    // Los errores solo se muestran después de tocar el campo, para no regañar al usuario antes de escribir.
    val codeError get() = if (codeTouched) validateCode(code) else null
    val passwordError get() = if (passwordTouched) validatePassword(password) else null
    val canSubmit get() = validateCode(code) == null && validatePassword(password) == null
}

// Eventos que suben desde la UI al contenedor ("props down, events up").
data class LoginActions(
    val onCodeChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onRememberChange: (Boolean) -> Unit,
    val onTogglePassword: () -> Unit,
    val onSubmit: () -> Unit
)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // Contenedor: guarda el estado con rememberSaveable (sobrevive a la rotación) y decide qué hacer al enviar.
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var codeTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }
    val state = LoginUiState(code, password, remember, showPassword, codeTouched, passwordTouched, loginError)
    val actions = LoginActions(
        onCodeChange = { code = sanitizeCode(it); codeTouched = true; loginError = null },
        onPasswordChange = { password = it; passwordTouched = true; loginError = null },
        onRememberChange = { remember = it },
        onTogglePassword = { showPassword = !showPassword },
        onSubmit = {
            codeTouched = true
            passwordTouched = true
            if (state.canSubmit) {
                if (checkCredentials(code, password)) {
                    // "Recordar" solo conserva el código en este formulario; la contraseña nunca se guarda.
                    if (!remember) code = ""
                    password = ""
                    onLoginSuccess()
                } else loginError = "Código o contraseña incorrectos. Revisa los datos e inténtalo otra vez."
            }
        }
    )
    LoginContent(state, actions)
}

@Composable
fun LoginContent(state: LoginUiState, actions: LoginActions) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageTitle("Ingresar a SisCome", "Usa tu código universitario. Demo: 20240001 / comedor123.")
        OutlinedTextField(
            value = state.code,
            onValueChange = actions.onCodeChange,
            label = { Text("Código universitario") },
            singleLine = true,
            isError = state.codeError != null,
            // supportingText muestra el error o una ayuda con el contador de dígitos.
            supportingText = { Text(state.codeError ?: "${state.code.length}/$CODE_LENGTH dígitos") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            isError = state.passwordError != null,
            supportingText = { Text(state.passwordError ?: "Mínimo 6 caracteres") },
            visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                // Mostrar/ocultar evita errores de tipeo sin exponer la contraseña por defecto.
                IconButton(onClick = actions.onTogglePassword) {
                    AppIcon(if (state.showPassword) "eye-off" else "eye", description = if (state.showPassword) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) actions.onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Recordar mi código", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = state.remember, onCheckedChange = actions.onRememberChange)
        }
        state.loginError?.let { Notice("No se pudo ingresar", it, error = true) }
        // El botón queda deshabilitado hasta que ambos campos son válidos: prevención de errores.
        Button(onClick = actions.onSubmit, enabled = state.canSubmit, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Ingresar")
        }
    }
}
