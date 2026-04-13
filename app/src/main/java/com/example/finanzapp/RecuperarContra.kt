package com.example.finanzapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showSystemUi = true)
@Composable
fun RecuperarContra(
    modifier: Modifier=Modifier,
    onClick:()-> Unit={}
){
    var email by remember { mutableStateOf("") }
    val modifierComponents= Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .defaultMinSize(minHeight = 48.dp)

    val colorComponents=TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF3F3F5),
        unfocusedContainerColor = Color(0xFFF3F3F5),
        disabledContainerColor = Color(0xFFF3F3F5),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )

    Column(
        modifier=modifier.fillMaxSize().imePadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedButton(
            onClick={},
            border= BorderStroke(2.dp,Color(0xFFF3F3F5)),
            colors = ButtonDefaults.buttonColors(
                Color.White,
                contentColor = Color.Black
            ),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Regresar"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver")
        }
        Surface(
            modifier = Modifier
                .imePadding()
                .padding(12.dp)
                .fillMaxWidth()
                .border(
                    2.dp,
                    color = Color(0xFF),
                    RoundedCornerShape(16.dp)
                )
                .shadow(8.dp, shape = RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier=Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F3F5))
                        .wrapContentWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lucide__mail), // ← Debe existir en drawable
                        contentDescription = "Icono",
                        modifier=modifier.size(40.dp)

                    )
                }
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text("Ingresa tu correo y te enviaremos instrucciones para recuperarla", style= MaterialTheme.typography.titleSmall,color=Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = email,
                    onValueChange = {email=it},
                    colors = colorComponents,
                    label ={Text("Correo Electrónico")},
                    placeholder = {Text("finanzapp@email.com")},
                    modifier=modifierComponents
                )
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        colorResource(R.color.ic_launcher_background),
                        contentColor = Color.White
                    ),
                    modifier=modifierComponents
                ) {Text("Enviar Instrucciones")}
            }
        }
    }
}
