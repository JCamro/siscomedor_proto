package com.example.siscomedor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siscomedor.ui.SisComeApp
import com.example.siscomedor.ui.theme.SisComeTheme

class MainActivity : ComponentActivity() {
    // Punto de entrada de Android: crea la ventana, no la sesión financiera ni los datos de cada pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // El contenido ocupa la ventana; Scaffold aplica después los márgenes del sistema.
        // setContent instala el árbol Compose. viewModel() reutiliza la sesión al recrear la Activity.
        // El tema envuelve todas las pantallas para compartir colores y roles tipográficos.
        setContent { SisComeTheme { SisComeApp(viewModel()) } }
    }
}
