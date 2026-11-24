package com.example.gestionregistroyvalidaciondeventas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionregistroyvalidaciondeventas.ui.venta.AdminScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.ListaVentasScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.LoginScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.MenuScreen
import com.example.gestionregistroyvalidaciondeventas.ui.venta.QrScannerScreen
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
            MenuScreen(
                navController = navController,
                viewModel = ventaViewModel
            )
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
        composable(
            route = "qrScanner/{ventaId}",
            arguments = listOf(navArgument("ventaId") { type = NavType.IntType })
        ) { backStackEntry ->
            QrScannerScreen(
                navController = navController,
                ventaViewModel = ventaViewModel,
                ventaIdAValidar = backStackEntry.arguments?.getInt("ventaId")
            )
        }
        composable("admin") {
            AdminScreen(
                navController = navController,
                viewModel = ventaViewModel
            )
        }
    }
}