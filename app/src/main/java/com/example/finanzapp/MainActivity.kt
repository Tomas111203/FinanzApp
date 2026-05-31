package com.example.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.finanzapp.ui.theme.FinanzAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            FinanzAppTheme() {
                ForceLightTheme {
                    App(auth = auth)
                }
            }
        }
    }
}

@Composable
fun App(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val currentUser by remember { mutableStateOf(auth.currentUser) }
    val transactionViewModel: TransactionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "Principal" else "Login"
    ) {
        // Pantallas de autenticación
        composable("Login") {
            LoginApp(
                auth = auth,
                navController = navController
            )
        }

        composable("CrearCuenta") {
            CrearCuenta(
                auth = auth,
                navController = navController
            )
        }

        composable("RecuperarContra") {
            RecuperarContra(
                auth = auth,
                navController = navController
            )
        }

        // Pantalla principal
        composable("Principal") {
            PantallaPrincipal(
                viewModel = transactionViewModel,
                navController = navController,
                user = auth.currentUser
            )
        }

        // NUEVAS RUTAS AGREGADAS

        // Ruta para Agregar Transacción
        composable("AgregarTransaccion") {
            AgregarTransaccionScreen(
                viewModel = transactionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Ruta para Historial
        composable("Historial") {
            HistorialScreen(
                viewModel = transactionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Ruta para Estadísticas
        composable("Estadisticas") {
            EstadisticasScreen(
                viewModel = transactionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PantallaPrincipal(
    viewModel: TransactionViewModel,
    navController: NavHostController,
    user: FirebaseUser?
) {
    TODO("Not yet implemented")
}

@Composable
fun LoginApp(
    auth: FirebaseAuth,
    navController: NavController
) {
    LoginScreen(
        auth = auth,
        modifier = Modifier.padding(),
        navController = navController,
        onCreateAccountClick = { navController.navigate("CrearCuenta") },
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