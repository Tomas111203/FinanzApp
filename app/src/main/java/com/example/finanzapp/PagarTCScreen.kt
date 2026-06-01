package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagarTCScreen(
    viewModel: TransactionViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val creditTransactions = transactions.filter {
        it.type == "expense" &&
                it.paymentMethod == "Tarjeta de Crédito" &&
                it.creditPaidSoFar < it.amount
    }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "MX")) }
    val scope = rememberCoroutineScope()
    var selectedTransaction by remember { mutableStateOf<FirestoreTransaction?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    // Calcular total de deuda
    val totalDebt = creditTransactions.sumOf { it.amount - it.creditPaidSoFar }
    val totalMonthlyPayment = creditTransactions.sumOf {
        (it.amount - it.creditPaidSoFar).coerceAtMost(it.amount / it.creditInstallments)
    }

    // Snackbar para mostrar errores
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Pagar Tarjeta de Crédito",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        containerColor = Color(0xFFF0FDF4),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (creditTransactions.isEmpty() && !isLoading) {
                    // Estado vacío
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "No tienes deudas pendientes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "¡Felicidades! No debes nada en tu tarjeta",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                } else if (creditTransactions.isNotEmpty()) {
                    // Tarjeta de resumen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Deuda Total",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "$${currencyFormatter.format(totalDebt)}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Próximo pago",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "$${currencyFormatter.format(totalMonthlyPayment)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Compras pendientes",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${creditTransactions.size}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lista de deudas
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(creditTransactions) { transaction ->
                            DebtCard(
                                transaction = transaction,
                                currencyFormatter = currencyFormatter,
                                onPayClick = {
                                    selectedTransaction = transaction
                                    showPaymentDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de pago - CORREGIDO
        if (showPaymentDialog && selectedTransaction != null) {
            PaymentDialog(
                transaction = selectedTransaction!!,
                currencyFormatter = currencyFormatter,
                isLoading = isLoading,
                onDismiss = {
                    showPaymentDialog = false
                    selectedTransaction = null
                },
                onConfirm = { amountToPay ->
                    scope.launch {
                        isLoading = true
                        try {
                            val result = viewModel.registerCreditCardPayment(
                                transactionId = selectedTransaction!!.id,
                                amount = amountToPay
                            )
                            if (result.isSuccess) {
                                showPaymentDialog = false
                                selectedTransaction = null
                                // Recargar datos
                                viewModel.loadTransactions()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar el pago"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DebtCard(
    transaction: FirestoreTransaction,
    currencyFormatter: NumberFormat,
    onPayClick: () -> Unit
) {
    val remaining = transaction.amount - transaction.creditPaidSoFar
    val monthlyPayment = transaction.amount / transaction.creditInstallments
    val progress = (transaction.creditPaidSoFar / transaction.amount).toFloat()
    val paidPercentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFF3B82F6).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (transaction.creditInstallments > 1) {
                            Text(
                                text = "${transaction.creditInstallments} meses",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                }

                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Pagar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pagar", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Montos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "$${currencyFormatter.format(transaction.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Restante", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "$${currencyFormatter.format(remaining)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progreso de pago", fontSize = 10.sp, color = Color.Gray)
                    Text("$paidPercentage%", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFFE5E7EB)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pagado: $${currencyFormatter.format(transaction.creditPaidSoFar)}",
                    fontSize = 11.sp,
                    color = Color(0xFF16A34A)
                )
                Text(
                    text = "Mensualidad: $${currencyFormatter.format(monthlyPayment)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PaymentDialog(
    transaction: FirestoreTransaction,
    currencyFormatter: NumberFormat,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var paymentAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val remaining = transaction.amount - transaction.creditPaidSoFar
    val monthlyPayment = transaction.amount / transaction.creditInstallments
    val suggestedPayment = minOf(monthlyPayment, remaining)

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Registrar Pago",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Información de la compra
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Compra", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                transaction.category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "$${currencyFormatter.format(transaction.amount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Restante", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "$${currencyFormatter.format(remaining)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // Campo de monto
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = {
                        paymentAmount = it
                        errorMessage = null
                    },
                    label = { Text("Monto a pagar") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, fontSize = 10.sp, color = Color.Red)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                // Pago sugerido
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading) {
                            paymentAmount = suggestedPayment.toString()
                            errorMessage = null
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E8FF)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Pago sugerido",
                            fontSize = 12.sp,
                            color = Color(0xFF6B21A8)
                        )
                        Text(
                            text = "$${currencyFormatter.format(suggestedPayment)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B21A8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val amount = paymentAmount.toDoubleOrNull()
                            when {
                                amount == null -> errorMessage = "Ingresa un monto válido"
                                amount <= 0 -> errorMessage = "El monto debe ser mayor a 0"
                                amount > remaining -> errorMessage = "El monto excede la deuda restante ($${currencyFormatter.format(remaining)})"
                                else -> {
                                    onConfirm(amount)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Pagar")
                        }
                    }
                }
            }
        }
    }
}