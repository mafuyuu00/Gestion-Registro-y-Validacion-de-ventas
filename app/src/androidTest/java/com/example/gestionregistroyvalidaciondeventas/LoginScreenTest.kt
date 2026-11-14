package com.example.gestionregistroyvalidaciondeventas

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionregistroyvalidaciondeventas.ui.venta.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreenDisplay() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginScreen(navController = navController)
        }

        // Verificar elementos en pantalla
        composeTestRule.onNodeWithText("Acceso Ejecutivo - 004D").assertExists()
        composeTestRule.onNodeWithText("Correo corporativo").assertExists()
        composeTestRule.onNodeWithText("Contraseña").assertExists()
        composeTestRule.onNodeWithText("Ingresar").assertExists()
    }
}
