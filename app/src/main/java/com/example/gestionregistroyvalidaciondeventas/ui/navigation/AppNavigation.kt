package com.example.gestionregistroyvalidaciondeventas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionregistroyvalidaciondeventas.ui.venta.ListaVentasScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.LoginScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.MenuScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.VentaFormScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.VentaViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val ventaViewModel: VentaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("menu") {
            MenuScreen(navController = navController)
        }
        composable("listaVentas") {
            ListaVentasScreen(
                navController = navController,
                viewModel = ventaViewModel
            )
        }
        composable("ventaForm") {
            VentaFormScreen(
                navController = navController,
                viewModel = ventaViewModel
            )
        }
    }
}