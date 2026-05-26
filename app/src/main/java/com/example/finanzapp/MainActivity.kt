package com.example.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.finanzapp.ui.theme.FinanzAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlin.toString

class MainActivity : ComponentActivity() {

    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth
        setContent {
            FinanzAppTheme(){
                ForceLightTheme {
                    App(auth = auth)
                }
            }
        }
    }
}

@Composable
fun App(auth: FirebaseAuth){
    val navController = rememberNavController()
    val currentUser by remember { mutableStateOf(auth.currentUser) }
    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "Principal" else "Login"
    ) {
        composable("Login") {
            LoginApp(
            auth,
                navController=navController
            )
        }
        composable("CrearCuenta"){
            CrearCuenta(
                auth,
                navController
            )
        }
        composable(route="Principal"){
            PantallaPrincipal(
                navController=navController,
                user= auth.currentUser
            )
        }
        composable(route="RecuperarContra") {
            RecuperarContra(
                auth,
                navController
            )
        }
    }
}

@Composable
fun LoginApp(
    auth: FirebaseAuth,
    navController: NavController
){
    LoginScreen(
        auth,
        modifier = Modifier.padding(),
        navController= navController,
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

