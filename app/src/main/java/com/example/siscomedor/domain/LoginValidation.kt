package com.example.siscomedor.domain

import com.example.siscomedor.data.DemoCatalog

// Validación pura (sin Compose): se puede probar con JUnit.
// Cada función devuelve el mensaje de error o null si el valor es válido.

const val CODE_LENGTH = 8
const val PASSWORD_MIN = 6

fun validateCode(code: String): String? = when {
    code.isBlank() -> "Ingresa tu código universitario."
    !code.all { it.isDigit() } -> "El código solo admite números."
    code.length != CODE_LENGTH -> "El código debe tener $CODE_LENGTH dígitos (llevas ${code.length})."
    else -> null
}

fun validatePassword(password: String): String? = when {
    password.isEmpty() -> "Ingresa tu contraseña."
    password.length < PASSWORD_MIN -> "La contraseña debe tener al menos $PASSWORD_MIN caracteres."
    else -> null
}

// Filtro de entrada: quita lo que no es dígito y corta en 8. Previene errores antes de que ocurran.
fun sanitizeCode(input: String): String = input.filter { it.isDigit() }.take(CODE_LENGTH)

fun checkCredentials(code: String, password: String): Boolean =
    code == DemoCatalog.demoCode && password == DemoCatalog.demoPassword
