package com.example.siscomedor.ui.login

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.siscomedor.ui.theme.SisComeTheme

// Acciones vacías: en Preview solo interesa ver cada estado del formulario.
private val noActions = LoginActions({}, {}, {}, {}, {})

@Composable
private fun LoginFrame(state: LoginUiState) {
    SisComeTheme { Surface(color = MaterialTheme.colorScheme.background) { LoginContent(state, noActions) } }
}

@Preview(name = "Login vacío", showBackground = true, widthDp = 400)
@Composable
private fun LoginEmptyPreview() = LoginFrame(LoginUiState())

@Preview(name = "Login con errores", showBackground = true, widthDp = 400)
@Composable
private fun LoginErrorsPreview() = LoginFrame(
    LoginUiState(code = "2024", password = "abc", codeTouched = true, passwordTouched = true)
)

@Preview(name = "Login credenciales incorrectas", showBackground = true, widthDp = 400)
@Composable
private fun LoginWrongPreview() = LoginFrame(
    LoginUiState(code = "20240002", password = "comedor123", codeTouched = true, passwordTouched = true,
        loginError = "Código o contraseña incorrectos. Revisa los datos e inténtalo otra vez.")
)

@Preview(name = "Login válido", showBackground = true, widthDp = 400)
@Composable
private fun LoginValidPreview() = LoginFrame(
    LoginUiState(code = "20240001", password = "comedor123", remember = true, codeTouched = true, passwordTouched = true)
)
