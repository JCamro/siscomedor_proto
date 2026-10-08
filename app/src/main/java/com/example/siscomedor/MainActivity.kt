package com.example.siscomedor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siscomedor.ui.SisComeApp
import com.example.siscomedor.ui.theme.SisComeTheme

class MainActivity : ComponentActivity() {
    // Punto de entrada de Android: crea la ventana
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SisComeTheme { SisComeApp(viewModel()) } }
    }
}
