package com.example.finanzapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun PantallaPrincipal(
    modifier: Modifier=Modifier,
    onClick:()-> Unit={}
){
    val modifierComponents= Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .defaultMinSize(minHeight = 48.dp)
    TopAppBar(
        title = {
            Row (verticalAlignment= Alignment.CenterVertically){
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.ic_launcher_background))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // ← Debe existir en drawable
                        contentDescription = "Icono",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(9.dp))
                Column() {
                    Text("FinanzApp", style = MaterialTheme.typography.titleLarge)
                    Text("Bienvenido de nuevo", style= MaterialTheme.typography.titleSmall,color=Color.Gray)
                }
                IconButton (onClick = {}){
                    Icon(
                        imageVector= Icons.Default.ExitToApp,
                        contentDescription="Logout"
                    )
                }
            }
        }
    )
}