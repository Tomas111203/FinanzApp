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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaPrincipal(
    viewModel: TransactionViewModel,
    navController: NavHostController,
    user: FirebaseUser?
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Carga inicial
    LaunchedEffect(Unit) {
        println("[Principal] Carga inicial...")
        if (transactions.isEmpty() && !isLoading) {
            viewModel.loadTransactions()
        }
    }

    //  Recargar cada vez que la pantalla se reanuda
    LifecycleResumeEffect(Unit) {
        println("[Principal] ON_RESUME - Recargando...")
        viewModel.loadTransactions()
        onPauseOrDispose { }
    }

    val userName = user?.displayName?.split(" ")?.firstOrNull() ?: "Usuario"

    // Calcular totales
    // Con este nuevo código:
    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }

// GASTOS REALES (los que ya salieron de tu cuenta)
    val realExpenses = transactions.filter {
        it.type == "expense" &&
                it.paymentMethod != "Tarjeta de Crédito"  // Solo efectivo y débito
    }.sumOf { it.amount }

// Pagos que ya hiciste de la tarjeta de crédito
    val creditCardTransfers = transactions.filter {
        it.type == "transfer" && it.paymentMethod == "Transferencia"
    }.sumOf { it.amount }

    val totalRealExpense = realExpenses + creditCardTransfers
    val totalBalance = totalIncome - totalRealExpense  // ✅ Balance REAL

// Para estadísticas (muestra el gasto completo aunque sea a crédito)
    val totalExpenseForStats = transactions.filter { it.type == "expense" }.sumOf { it.amount }

    val totalCreditDebt = transactions.filter {
        it.type == "expense" &&
                it.paymentMethod == "Tarjeta de Crédito"
    }.sumOf { it.amount - it.creditPaidSoFar }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "MX")) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale("es", "MX")) }

    val recentTransactions = transactions.take(5)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // TopBar
            item {
                TopBar(userName = userName, navController = navController)
            }

            // Balance
            item {
                Balance(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalRealExpense,
                    currencyFormatter = currencyFormatter,
                    totalCreditDebt = totalCreditDebt,
                    transactions = transactions
                )
            }

            // Botones
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, start=2.dp, bottom=2.dp)
                ) {
                    Botones("Agregar", painterResource(R.drawable.mdi), Color(0xFF00A63E)) {
                        navController.navigate("AgregarTransaccion")
                    }
                    Botones(
                        "Historial",
                        painterResource(R.drawable.fluent__history_32_filled),
                        Color.Blue
                    ) {
                        navController.navigate("Historial")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, end= 2.dp, bottom=2.dp)
                ) {
                    Botones("Estadisticas", painterResource(R.drawable.lucide__chart_column), Color.Magenta) {
                        navController.navigate("Estadisticas")
                    }
                    Botones("Pagar TC",painterResource(R.drawable.lucide__credit_card), Color(0xFF3B82F6)
                    ) {
                        navController.navigate("PagarTC")
                    }

                }
            }

            // Transacciones recientes
            item {
                Surface(
                    modifier = Modifier
                        .imePadding()
                        .padding(15.dp)
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF), RoundedCornerShape(16.dp))
                        .shadow(8.dp, shape = RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text(
                            "Transacciones Recientes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        when {
                            // Mostrar transacciones si hay
                            recentTransactions.isNotEmpty() -> {
                                recentTransactions.forEach { transaction ->
                                    // Determinar si es tarjeta de crédito
                                    val esTC = transaction.paymentMethod == "Tarjeta de Crédito"
                                    val esTransferenciaTC = transaction.type == "transfer" && transaction.paymentMethod == "Transferencia"
                                    val esPagoTC = esTransferenciaTC  // Para mayor claridad

                                    // Para transferencias de pago de TC, no mostrar signo
                                    val montoTexto = when {
                                        transaction.type == "income" -> {
                                            "+$${currencyFormatter.format(transaction.amount)}"
                                        }
                                        esPagoTC -> {
                                            "-$${currencyFormatter.format(transaction.amount)}"  // ← Con signo -
                                        }
                                        esTC -> {
                                            "$${currencyFormatter.format(transaction.amount)}"  // ← Sin signo, solo el monto
                                        }
                                        else -> {
                                            "-$${currencyFormatter.format(transaction.amount)}"  // Gastos normales con -
                                        }
                                    }

                                    // Para transacciones de TC, mostrar información adicional
                                    val mostrarInfoTC = esTC && transaction.creditInstallments > 1

                                    ItemTransaccion(
                                        categoria = if (esTransferenciaTC) "Pago Tarjeta" else transaction.name,
                                        descripcion = if (esTransferenciaTC) transaction.description else transaction.category,
                                        monto = montoTexto,
                                        fecha = dateFormatter.format(transaction.date),
                                        ingreso = transaction.type == "income",
                                        esTarjetaCredito = esTC,
                                        meses = if (esTC) transaction.creditInstallments else 1,
                                        pagado = if (esTC) transaction.creditPaidSoFar else 0.0,
                                        total = if (esTC) transaction.amount else 0.0
                                    )
                                }
                            }
                            // Mostrar loader solo en primera carga sin datos
                            isLoading && transactions.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            // Mostrar mensaje si no hay transacciones
                            else -> {
                                Text(
                                    "No hay transacciones aún",
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Indicador de carga overlay (solo cuando recarga pero ya hay datos)
        if (isLoading && transactions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
        }

        // Error message
        errorMessage?.let { error ->
            androidx.compose.material3.Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.loadTransactions() }) {
                        Text("Reintentar")
                    }
                }
            ) {
                Text(error)
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
            .border(2.dp, Color(0xFFE0E0E0), shape = RectangleShape)
            .shadow(8.dp, shape = RectangleShape, clip = false),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
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
                        Text("Bienvenido $userName", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
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
    currencyFormatter: NumberFormat,
    totalCreditDebt: Double,
    transactions: List<FirestoreTransaction>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .height(180.dp)
            .border(2.dp, colorResource(R.color.ic_launcher_background), RoundedCornerShape(16.dp)),
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
                modifier = Modifier.padding(15.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text("Balance Total", style = MaterialTheme.typography.titleSmall, color = Color.White)
                Text(
                    "$${currencyFormatter.format(totalBalance)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                if (totalCreditDebt > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Deuda TC: $${currencyFormatter.format(totalCreditDebt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "Próximo pago: $${"%.2f".format(calculateNextPayment(transactions))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
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
                            Text("Ingresos", style = MaterialTheme.typography.titleSmall, color = Color.White)
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
                            Text("Gastos", style = MaterialTheme.typography.titleSmall, color = Color.White)
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
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        modifier = Modifier
            .height(85.dp)
            .width(165.dp)
            .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
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
                    .background(color = colorResource(R.color.fondo_icono), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = icon, contentDescription = text, modifier = Modifier.size(18.dp), tint = tint)
            }
            Text(
                text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().wrapContentSize(),
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
    ingreso: Boolean,
    esTarjetaCredito: Boolean = false,  // NUEVO
    meses: Int = 1,  // NUEVO
    pagado: Double = 0.0,  // NUEVO
    total: Double = 0.0  // NUEVO
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Mostrar categoría con indicador de MSI si aplica
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(categoria, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (esTarjetaCredito && meses > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "($meses MSI)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            // Mostrar progreso de pago para TC
            if (esTarjetaCredito && meses > 1 && pagado < total) {
                val porcentaje = (pagado / total * 100).toInt()
                Text(
                    "Pagado: $porcentaje% • ${(total - pagado).toInt()} restante",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    fontSize = 10.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                monto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    esTarjetaCredito -> Color(0xFF3B82F6)  // Color azul para TC
                    ingreso -> Color.Green
                    else -> Color.Red
                }
            )
            Text(fecha, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}


// Agrega esta función auxiliar en PantallaPrincipal (fuera del @Composable)
fun calculateNextPayment(transactions: List<FirestoreTransaction>): Double {
    val creditTransactions = transactions.filter {
        it.type == "expense" &&
                it.paymentMethod == "Tarjeta de Crédito" &&
                it.creditPaidSoFar < it.amount
    }

    var nextPayment = 0.0
    creditTransactions.forEach { transaction ->
        val remaining = transaction.amount - transaction.creditPaidSoFar
        val monthlyPayment = transaction.amount / transaction.creditInstallments
        nextPayment += minOf(monthlyPayment, remaining)
    }

    return nextPayment
}