package com.example.gestionregistroyvalidaciondeventas

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.gestionregistroyvalidaciondeventas.ui.venta.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun los_elementos_del_login_deben_aparecer_en_pantalla() {
        composeRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeRule.onNodeWithText("Acceso Ejecutivo - 004D").assertIsDisplayed()
        composeRule.onNodeWithText("Correo corporativo").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun con_datos_invalidos_debe_mostrar_el_mensaje_de_error() {
        composeRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeRule.onNodeWithText("Correo corporativo").performTextInput("usuario")
        composeRule.onNodeWithText("Contraseña").performTextInput("12")
        composeRule.onNodeWithText("Ingresar").performClick()

        composeRule.onNodeWithText("Ingrese un correo válido y clave (mín. 4 caracteres)").assertIsDisplayed()
    }
}
