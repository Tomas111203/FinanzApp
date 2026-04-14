package com.example.finanzapp


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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview(showSystemUi = true)
@Composable
fun PantallaPrincipal(
    onClick:()-> Unit={}
){
        Column(

        ) {
            TopBar()
            Balance()
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(15.dp)

            ) {
                Botones(
                    text = "Agregar",
                    icon = painterResource(R.drawable.mdi),
                    tint = Color.Green,
                    onClick={}
                )
                Botones(
                    text = "Historial",
                    icon = painterResource(R.drawable.fluent__history_32_filled),
                    tint = Color.Blue,
                    onClick={}
                )
                Botones(
                    text = "Estadisticas",
                    icon = painterResource(R.drawable.lucide__chart_column),
                    tint = Color.Magenta,
                    onClick={}
                )
            }
            Historial()
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(){
    TopAppBar(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .border(
                2.dp,
                color = Color(0xFFE0E0E0),
                shape = RectangleShape
            )
            .shadow(
                8.dp,
                shape = RectangleShape,
                clip = false
            ),
        title = {
            Row (verticalAlignment= Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp))
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorResource(R.color.ic_launcher_background))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // ← Debe existir en drawable
                            contentDescription = "Icono",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Column() {
                        Text("FinanzApp", style = MaterialTheme.typography.titleLarge)
                        Text("Bienvenido de nuevo", style= MaterialTheme.typography.titleSmall,color=Color.Gray)
                    }
                }
                IconButton (onClick = {}){
                    Icon(
                        painter=painterResource( R.drawable.ic__outline_logout),
                        contentDescription="Logout"
                    )
                }
            }
        }
    )
}

@Composable
fun Balance(){
    Surface(
        modifier= Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .height(180.dp)
            .border(
                2.dp,
                colorResource(R.color.ic_launcher_background),
                RoundedCornerShape(16.dp)
            ),
        color= Color.Transparent,
        shape = RoundedCornerShape(16.dp),

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colorResource(R.color.verde_lima), colorResource(R.color.ic_launcher_background)),
                        start = Offset(0f, 0f), // Esquina superior izquierda
                        end = Offset(1000f, 1000f) // Esquina inferior derecha
                    )
                )
        )
        {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround

            ) {
                Text(
                    "Balance Total",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Text(
                    "$15,420.50",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column() {
                        Row(
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.icon_park_outline__trending_up),
                                contentDescription = "Ingresos",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Text(
                                "Ingresos",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }
                        Text(
                            "$18,701.25",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column() {
                        Row()
                        {
                            Icon(
                                painter = painterResource(R.drawable.icon_park_outline__trending_down),
                                contentDescription = "Gastos",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Text(
                                "Gastos",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }
                        Text(
                            "$3,280.75",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Botones(
    text: String,
    icon: Painter,
    tint : Color,
    onClick:()-> Unit
){
    Button(
    onClick=onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier=Modifier
            .height(85.dp)
            .width(115.dp)
            .border(
                2.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp),
            )
            .shadow(8.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ){
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = colorResource(R.color.fondo_icono),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = text,
                    modifier = Modifier.size(18.dp),
                    tint = tint
                )
            }
            Text(
                text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(),
                fontSize = 12.sp,
                softWrap = false
            )
        }
    }
}

@Composable
fun Historial(){
    Surface(
        modifier = Modifier
            .imePadding()
            .padding(15.dp)
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
            modifier=Modifier.padding(15.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Transacciones Recientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                ItemTransaccion(
                    "Supermercado",
                    "Alimentación",
                    "$450.5",
                    "28 Mar",
                    false
                )
                ItemTransaccion(
                    "Salario",
                    "Ingreso",
                    "+$18,000",
                    "25 Mar",
                    true
                )
                ItemTransaccion(
                    "Gasolina",
                    "Transporte",
                    "$280",
                    "27 Mar",
                    false
                )
                ItemTransaccion(
                    "Netflix",
                    "Entretenimiento",
                    "$129",
                    "26 Mar",
                    false
                )
            }
        }
    }
}

@Composable
fun ItemTransaccion(
    categoria: String,
    descripcion: String,
    monto: String,
    fecha: String,
    ingreso: Boolean
){
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(categoria, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(monto ,style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if(ingreso) Color.Green else Color.Red)
            Text(fecha, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}