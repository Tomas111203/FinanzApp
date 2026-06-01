package com.example.finanzapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val creditTransactions = transactions.filter {
        it.type == "expense" &&
                it.paymentMethod == "Tarjeta de Crédito" &&
                it.creditPaidSoFar < it.amount // No completamente pagadas
    }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "MX")) }
    val scope = rememberCoroutineScope()
    var selectedTransaction by remember { mutableStateOf<FirestoreTransaction?>(null) }
    var paymentAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagar Tarjeta de Crédito") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (creditTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes deudas pendientes en tarjeta de crédito")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(creditTransactions) { transaction ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTransaction = transaction }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = transaction.category,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Total: $${currencyFormatter.format(transaction.amount)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Pagado: $${currencyFormatter.format(transaction.creditPaidSoFar)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Restante: $${currencyFormatter.format(transaction.amount - transaction.creditPaidSoFar)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mensualidad: $${currencyFormatter.format(transaction.amount / transaction.creditInstallments)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Diálogo de pago
                if (selectedTransaction != null) {
                    AlertDialog(
                        onDismissRequest = { selectedTransaction = null },
                        title = { Text("Registrar pago") },
                        text = {
                            Column {
                                Text("Compra: ${selectedTransaction!!.category}")
                                Text("Total: $${currencyFormatter.format(selectedTransaction!!.amount)}")
                                Text("Pagado: $${currencyFormatter.format(selectedTransaction!!.creditPaidSoFar)}")
                                Text("Restante: $${currencyFormatter.format(selectedTransaction!!.amount - selectedTransaction!!.creditPaidSoFar)}")

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = paymentAmount,
                                    onValueChange = { paymentAmount = it },
                                    label = { Text("Monto a pagar") },
                                    placeholder = { Text("Ej: 1000") },
                                    leadingIcon = { Text("$") }
                                )

                                val monthlyPayment = selectedTransaction!!.amount / selectedTransaction!!.creditInstallments
                                Text(
                                    text = "Pago sugerido: $${currencyFormatter.format(monthlyPayment)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val amountToPay = paymentAmount.toDoubleOrNull()
                                    if (amountToPay != null && amountToPay > 0) {
                                        scope.launch {
                                            viewModel.registerCreditCardPayment(
                                                transactionId = selectedTransaction!!.id,
                                                amount = amountToPay
                                            )
                                            selectedTransaction = null
                                            paymentAmount = ""
                                        }
                                    }
                                }
                            ) {
                                Text("Pagar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { selectedTransaction = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}