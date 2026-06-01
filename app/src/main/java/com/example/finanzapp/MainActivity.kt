package com.example.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finanzapp.ui.theme.FinanzAppTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        println("[MainActivity] onCreate - Iniciando aplicación")
        println("[MainActivity] Usuario actual: ${auth.currentUser?.email ?: "No hay usuario logueado"}")

        setContent {
            FinanzAppTheme() {
                ForceLightTheme {
                    App(auth = auth)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        println("[MainActivity] onStart - App en primer plano")
    }

    override fun onStop() {
        super.onStop()
        println("[MainActivity] onStop - App en segundo plano")
    }
}

@Composable
fun App(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val currentUser by remember { mutableStateOf(auth.currentUser) }

    // Logs de navegación
    LaunchedEffect(currentUser) {
        println("[App] Usuario actual: ${currentUser?.email ?: "No logueado"}")
        println("[App] UID: ${currentUser?.uid ?: "Ninguno"}")
    }

    // Crear el ViewModel compartido
    println("[App] Creando TransactionViewModel...")
    val transactionViewModel: TransactionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "Principal" else "Login"
    ) {
        println("[App] NavHost - StartDestination: ${if (currentUser != null) "Principal" else "Login"}")

        // Pantallas de autenticación
        composable("Login") {
            println("[Nav] Navegando a Login")
            LoginApp(
                auth = auth,
                navController = navController
            )
        }

        composable("CrearCuenta") {
            println("[Nav] Navegando a CrearCuenta")
            CrearCuenta(
                auth = auth,
                navController = navController
            )
        }

        composable("RecuperarContra") {
            println("[Nav] Navegando a RecuperarContra")
            RecuperarContra(
                auth = auth,
                navController = navController
            )
        }

        // Pantalla principal
        composable("Principal") {
            println("[Nav] Navegando a Pantalla Principal")
            PantallaPrincipal(
                viewModel = transactionViewModel,
                navController = navController,
                user = auth.currentUser
            )
        }

        // Agregar Transacción
        composable("AgregarTransaccion") {
            println("[Nav] Navegando a Agregar Transacción")
            AgregarTransaccionScreen(
                viewModel = transactionViewModel,
                onBackClick = {
                    println("[Nav] Regresando desde Agregar Transacción (back)")
                    println("[Nav] Recargando datos en Principal...")
                    transactionViewModel.loadTransactions()
                    navController.popBackStack()
                },
                onSaveClick = {
                    println("[Nav] Transacción guardada, regresando...")
                    println("[Nav] Recargando datos en Principal...")
                    transactionViewModel.loadTransactions()
                    navController.popBackStack()
                }
            )
        }

        // Historial
        composable("Historial") {
            println("[Nav] Navegando a Historial")
            HistorialScreen(
                viewModel = transactionViewModel,
                onBackClick = {
                    println("[Nav] Regresando desde Historial")
                    navController.popBackStack()
                }
            )
        }

        // Estadísticas
        composable("Estadisticas") {
            println("[Nav] Navegando a Estadísticas")
            EstadisticasScreen(
                viewModel = transactionViewModel,
                onBackClick = {
                    println("[Nav] Regresando desde Estadísticas")
                    navController.popBackStack()
                }
            )
        }

        composable("PagarTC") {
            PagarTCScreen(
                viewModel = transactionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}

@Composable
fun LoginApp(
    auth: FirebaseAuth,
    navController: androidx.navigation.NavController
) {
    println("[LoginApp] Mostrando pantalla de login")
    LoginScreen(
        auth = auth,
        modifier = androidx.compose.ui.Modifier.padding(),
        navController = navController,
        onCreateAccountClick = {
            println("[LoginApp] Click en crear cuenta")
            navController.navigate("CrearCuenta")
        },
    )
}

@Composable
fun ForceLightTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        content()
    }
}