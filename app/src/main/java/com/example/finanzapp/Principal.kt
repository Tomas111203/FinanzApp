package com.example.finanzapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaPrincipal(
    navController: NavController,
    user: FirebaseUser?,
    onClick: () -> Unit = {}
) {
    val repository = remember { TransactionRepository() }
    var transactions by remember { mutableStateOf<List<FirestoreTransaction>>(emptyList()) }
    val userName = user?.displayName?.split(" ")?.firstOrNull() ?: "Usuario"

    // Cargar transacciones reales desde Firestore
    LaunchedEffect(Unit) {
        try {
            transactions = repository.getAllTransactions()
        } catch (e: Exception) {
            // Si hay error, se muestra lista vacía
            transactions = emptyList()
        }
    }

    // Calcular totales reales
    val totalIncome = transactions
        .filter { it.type == "income" }
        .sumOf { it.amount }
    val totalExpense = transactions
        .filter { it.type == "expense" }
        .sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale("es", "MX"))
    }
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM", Locale("es", "MX"))
    }

    // Obtener solo las últimas 5 transacciones para mostrar
    val recentTransactions = transactions.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopBar
        item {
            TopBar(userName = userName, navController = navController)
        }

        // Balance
        item {
            Balance(
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                currencyFormatter = currencyFormatter
            )
        }

        // Botones de navegación
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Botones(
                    text = "Agregar",
                    icon = painterResource(R.drawable.mdi),
                    tint = Color.Green,
                    onClick = {
                        navController.navigate("AgregarTransaccion")
                    }
                )
                Botones(
                    text = "Historial",
                    icon = painterResource(R.drawable.fluent__history_32_filled),
                    tint = Color.Blue,
                    onClick = {
                        navController.navigate("Historial")
                    }
                )
                Botones(
                    text = "Estadisticas",
                    icon = painterResource(R.drawable.lucide__chart_column),
                    tint = Color.Magenta,
                    onClick = {
                        navController.navigate("Estadisticas")
                    }
                )
            }
        }

        // Transacciones recientes
        item {
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
                    modifier = Modifier.padding(15.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Transacciones Recientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (recentTransactions.isEmpty()) {
                        Text(
                            "No hay transacciones aún",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    } else {
                        recentTransactions.forEach { transaction ->
                            ItemTransaccion(
                                categoria = transaction.name,
                                descripcion = transaction.category,
                                monto = if (transaction.type == "income")
                                    "+$${currencyFormatter.format(transaction.amount)}"
                                else
                                    "-$${currencyFormatter.format(transaction.amount)}",
                                fecha = dateFormatter.format(transaction.date),
                                ingreso = transaction.type == "income"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(userName: String, navController: NavController) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorResource(R.color.ic_launcher_background))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Icono",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text("FinanzApp", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Bienvenido $userName",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("Login") {
                        popUpTo("Principal") { inclusive = true }
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic__outline_logout),
                        contentDescription = "Logout"
                    )
                }
            }
        }
    )
}

@Composable
fun Balance(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencyFormatter: NumberFormat
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .height(180.dp)
            .border(
                2.dp,
                colorResource(R.color.ic_launcher_background),
                RoundedCornerShape(16.dp)
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.verde_lima),
                            colorResource(R.color.ic_launcher_background)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
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
                    "$${currencyFormatter.format(totalBalance)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.Start) {
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
                            "$${currencyFormatter.format(totalIncome)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Row {
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
                            "$${currencyFormatter.format(totalExpense)}",
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
    tint: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .height(85.dp)
            .width(115.dp)
            .border(
                2.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp),
            )
            .shadow(8.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
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
fun ItemTransaccion(
    categoria: String,
    descripcion: String,
    monto: String,
    fecha: String,
    ingreso: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                categoria,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                monto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (ingreso) Color.Green else Color.Red
            )
            Text(
                fecha,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}