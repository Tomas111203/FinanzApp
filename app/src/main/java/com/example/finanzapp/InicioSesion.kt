package com.example.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider


@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    modifier: Modifier=Modifier,
    navController: NavController,
    onCreateAccountClick: () -> Unit = {}
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val token= stringResource(R.string.default_web_client_id)
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    var showDialog by remember { mutableStateOf(false) }
    var message by remember {mutableStateOf("")}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts
            .StartActivityForResult()
    ){
        val task= GoogleSignIn.getSignedInAccountFromIntent(it.data)
        val account=task.getResult(ApiException::class.java)
        val credential= GoogleAuthProvider.getCredential(
            account.idToken,
            null
        )

        auth
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    message="Inicio de Sesion exitoso"
                    navController.navigate("Principal") {
                        popUpTo("Login") { inclusive = true }
                    }
                    currentUser = auth.currentUser
                }else{
                    showDialog=true
                    val exception= task.exception
                    message = when (exception) {
                        is ApiException -> when (exception.statusCode) {
                            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Inicio de sesión cancelado"
                            GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Error al iniciar sesión con Google"
                            GoogleSignInStatusCodes.NETWORK_ERROR -> "Error de red. Verifica tu conexión"
                            else -> "Error: ${exception.message}"
                        }
                        is FirebaseAuthInvalidCredentialsException -> "Credenciales de Google inválidas"
                        else -> "Error: ${exception?.message}"
                    }
                }
                currentUser=auth.currentUser
            }
    }

    val colorComponents=TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF3F3F5),
        unfocusedContainerColor = Color(0xFFF3F3F5),
        disabledContainerColor = Color(0xFFF3F3F5),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
    val modifierComponents= Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .defaultMinSize(minHeight = 48.dp)
    Column(
        modifier=modifier.fillMaxSize().imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colorResource(R.color.ic_launcher_background))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // ← Debe existir en drawable
                contentDescription = "Icono",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text("FinanzApp", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(5.dp))
        Text("Controla tus finanzas personales", style= MaterialTheme.typography.titleSmall,color=Color.Gray)
        Spacer(Modifier.height(50.dp))
        Surface(
            modifier= Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(
                    2.dp,
                    color=Color(0xFF),
                    RoundedCornerShape(16.dp))
                .shadow(8.dp,shape=RoundedCornerShape(20.dp)),
            color= MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ){
            Column(
                modifier=Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isEmailValid=email.matches(Regex("^(?=.*[A-Za-z])(?=.*[@])[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))
                TextField(
                    value = email,
                    onValueChange = {email=it},
                    colors = colorComponents,
                    label ={Text("Correo Electrónico")},
                    placeholder = {Text("finanzapp@email.com")},
                    modifier=modifierComponents,
                    isError = !isEmailValid

                )
                Spacer(modifier=Modifier.width(10.dp))
                TextField(
                    value = password,
                    onValueChange = {password=it},
                    colors = colorComponents,
                    label ={Text("Contraseña")},
                    placeholder = {Text("******")},
                    modifier=modifierComponents,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = if(passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon={
                        val id= if(passwordVisible)
                            R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24

                        IconButton(
                            onClick = {passwordVisible = !passwordVisible},
                            modifier= Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter=painterResource(id),
                                contentDescription= if(passwordVisible)
                                    "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        auth
                            .signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener {task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser

                                    if (user?.isEmailVerified == true) {
                                        currentUser = auth.currentUser
                                        navController.navigate("Principal") {
                                            popUpTo("Login") { inclusive = true }
                                        }
                                    } else {
                                        auth.signOut()
                                        message = "Por favor, verifica tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada y spam."
                                        showDialog = true
                                    }
                                }else{
                                    message = when (task.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
                                        is FirebaseAuthInvalidUserException -> "No existe cuenta con este correo"
                                        else -> "Error: ${task.exception?.message}"
                                    }

                                    showDialog=true
                                }

                                currentUser=auth.currentUser
                            }
                    },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        colorResource(R.color.ic_launcher_background),
                        contentColor = Color.White
                    ),
                    modifier = modifierComponents
                        .shadow(
                            elevation = 4.dp, // Altura de la sombra
                            shape = RoundedCornerShape(24.dp), // Forma de la sombra (ajústala según tu botón)
                            clip = true // Para que no recorte la sombra
                        )
                ) {Text("Entrar")}
                OutlinedButton(
                    onClick = onCreateAccountClick,
                    border= BorderStroke(2.dp,Color(0xFFF3F3F5)),
                    colors = ButtonDefaults.buttonColors(
                        Color.White,
                        contentColor = Color.Black
                    ),modifier = modifierComponents
                        .shadow(
                            elevation = 4.dp, // Altura de la sombra
                            shape = RoundedCornerShape(24.dp), // Forma de la sombra (ajústala según tu botón)
                            clip = true // Para que no recorte la sombra
                        )
                ) {Text("Crear Cuenta")}
                OutlinedButton(
                    onClick = {
                        val options = GoogleSignInOptions.Builder(
                            GoogleSignInOptions.DEFAULT_SIGN_IN
                        )
                            .requestIdToken(token)
                            .requestEmail()
                            .build()

                        val googleSingInClient= GoogleSignIn.getClient(
                            context,
                            options
                        )
                        launcher.launch(googleSingInClient.signInIntent)
                    },
                    border = BorderStroke(2.dp, Color(0xFFF3F3F5)),
                    colors = ButtonDefaults.buttonColors(
                        Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = modifierComponents
                        .shadow(
                            elevation = 4.dp, // Altura de la sombra
                            shape = RoundedCornerShape(24.dp), // Forma de la sombra (ajústala según tu botón)
                            clip = true // Para que no recorte la sombra
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.logo_google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Iniciar sesión con Google")
                    }
                }
                Spacer(Modifier.height(8.dp))
                ClickableText(
                    text = AnnotatedString("¿Olvidaste la contraseña?"),
                    style= TextStyle(
                        color=colorResource(R.color.ic_launcher_background),
                        textDecoration = TextDecoration.Underline
                    )
                ) { navController.navigate("RecuperarContra")}
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = if (message.contains("exitoso") || message.contains("Correcto")) "Éxito" else "Error",
                        fontWeight = FontWeight.Bold,
                        color = if (message.contains("exitoso") || message.contains("Correcto"))
                            Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                },
                text = {
                    Text(text = message)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            if (message.contains("exitoso")|| message.contains("Correcto")){
                                currentUser = auth.currentUser
                                navController.navigate("Principal") {
                                    popUpTo("Login") { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF6200EE)
                        )
                    ) {
                        Text("Aceptar")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

