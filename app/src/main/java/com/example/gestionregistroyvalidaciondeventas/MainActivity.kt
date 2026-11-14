package com.example.gestionregistroyvalidaciondeventas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.gestionregistroyvalidaciondeventas.ui.navigation.AppNavigation
import com.example.gestionregistroyvalidaciondeventas.ui.theme.GestionregistroyvalidaciondeventasTheme
import com.example.gestionregistroyvalidaciondeventas.ui.venta.VentaViewModel

class MainActivity : ComponentActivity() {
    private val ventaViewModel: VentaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by ventaViewModel.isDarkMode.collectAsState()

            GestionregistroyvalidaciondeventasTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
