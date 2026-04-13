package com.example.finanzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showSystemUi = true)
@Composable
fun LoginScreen(
    modifier: Modifier=Modifier,
    onClick:()-> Unit={}
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                TextField(
                    value = email,
                    onValueChange = {email=it},
                    colors = colorComponents,
                    label ={Text("Correo Electrónico")},
                    placeholder = {Text("finanzapp@email.com")},
                    modifier=modifierComponents

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
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        colorResource(R.color.ic_launcher_background),
                        contentColor = Color.White
                    ),
                    modifier=modifierComponents
                ) {Text("Entrar")}
                OutlinedButton(
                    onClick = onClick,
                    border= BorderStroke(2.dp,Color(0xFFF3F3F5)),
                    colors = ButtonDefaults.buttonColors(
                        Color.White,
                        contentColor = Color.Black
                    ),
                    modifier=modifierComponents
                ) {Text("Crear Cuenta")}
                ClickableText(
                    text = AnnotatedString("¿Olvidaste la contraseña?"),
                    style= TextStyle(
                        color=colorResource(R.color.ic_launcher_background),
                        textDecoration = TextDecoration.Underline
                    )
                ) { }
            }
        }

    }
}

