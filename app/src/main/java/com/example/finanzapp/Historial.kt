package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBackClick: () -> Unit = {}
) {
    val repository = remember { TransactionRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Estados
    var transactions by remember { mutableStateOf<List<FirestoreTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchTerm by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas las categorías") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("income") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "Todas las categorías",
        "Alimentación",
        "Transporte",
        "Entretenimiento",
        "Salud",
        "Servicios",
        "Educación",
        "Ropa",
        "Ingreso",
        "Otros"
    )

    // Cargar transacciones al iniciar
    LaunchedEffect(Unit) {
        try {
            transactions = repository.getAllTransactions()
        } catch (e: Exception) {
            errorMessage = "Error al cargar transacciones: ${e.message}"
            showErrorDialog = true
        } finally {
            isLoading = false
        }
    }

    // Filtrar transacciones según búsqueda y categoría
    val filteredTransactions = transactions.filter { transaction ->
        val matchesSearch = transaction.name.lowercase()
            .contains(searchTerm.lowercase()) ||
                transaction.category.lowercase()
                    .contains(searchTerm.lowercase())
        val matchesCategory = selectedCategory == "Todas las categorías" ||
                transaction.category == selectedCategory
        matchesSearch && matchesCategory
    }

    // Calcular totales
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
        SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
    }

    // Diálogo para agregar transacción
    if (showAddDialog) {
        AddTransactionDialog(
            type = transactionType,
            onDismiss = { showAddDialog = false },
            onAdd = { transactionInput ->
                coroutineScope.launch {
                    try {
                        val firestoreTransaction = FirestoreTransaction(
                            name = transactionInput.name,
                            amount = transactionInput.amount,
                            category = transactionInput.category,
                            date = Date(),
                            type = transactionInput.type,
                            description = transactionInput.description,
                            paymentMethod = transactionInput.paymentMethod
                        )

                        val result = repository.addTransaction(firestoreTransaction)
                        result.onSuccess {
                            // Recargar transacciones después de agregar
                            transactions = repository.getAllTransactions()
                            showAddDialog = false
                        }.onFailure { e ->
                            errorMessage = "Error al guardar: ${e.message}"
                            showErrorDialog = true
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                        showErrorDialog = true
                    }
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Error",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Historial",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    // Botones para agregar transacciones
                    IconButton(
                        onClick = {
                            transactionType = "income"
                            showAddDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Ingreso",
                            tint = Color(0xFF16A34A)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            transactionType = "expense"
                            showAddDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Agregar Gasto",
                            tint = Color(0xFFDC2626)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF0FDF4)
    ) { paddingValues ->
        if (isLoading) {
            // Mostrar indicador de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF16A34A)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    placeholder = { Text("Buscar transacción...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtro por categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { showCategoryDropdown = true }
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCategory,
                                    color = if (selectedCategory == "Todas las categorías")
                                        Color.Gray else Color.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar",
                                    tint = Color.Gray
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de resumen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ingresos", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "+$${currencyFormatter.format(totalIncome)}",
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gastos", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "-$${currencyFormatter.format(totalExpense)}",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Balance", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "$${currencyFormatter.format(totalBalance)}",
                                color = if (totalBalance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de transacciones
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    if (filteredTransactions.isEmpty()) {
                        // Estado vacío
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No se encontraron transacciones",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredTransactions,
                                key = { it.id }
                            ) { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    currencyFormatter = currencyFormatter,
                                    dateFormatter = dateFormatter,
                                    onDelete = {
                                        coroutineScope.launch {
                                            try {
                                                val result = repository.deleteTransaction(transaction.id)
                                                result.onSuccess {
                                                    transactions = repository.getAllTransactions()
                                                }.onFailure { e ->
                                                    errorMessage = "Error al eliminar: ${e.message}"
                                                    showErrorDialog = true
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Error: ${e.message}"
                                                showErrorDialog = true
                                            }
                                        }
                                    }
                                )
                                if (transaction != filteredTransactions.last()) {
                                    HorizontalDivider(
                                        color = Color(0xFFE5E7EB),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente individual de transacción
@Composable
fun TransactionItem(
    transaction: FirestoreTransaction,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Eliminar transacción",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text("¿Deseas eliminar \"${transaction.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (transaction.type == "income")
                    Color(0xFF16A34A).copy(alpha = 0.1f)
                else
                    Color(0xFFDC2626).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.type == "income")
                            Icons.Default.ArrowUpward
                        else
                            Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (transaction.type == "income")
                            Color(0xFF16A34A)
                        else
                            Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = transaction.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = transaction.category,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormatter.format(transaction.date),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Text(
            text = if (transaction.type == "income")
                "+$${currencyFormatter.format(transaction.amount)}"
            else
                "-$${currencyFormatter.format(transaction.amount)}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (transaction.type == "income") Color(0xFF16A34A) else Color(0xFFDC2626)
        )
    }
}

// Modelo para el diálogo
data class TransactionInput(
    val name: String,
    val amount: Double,
    val category: String,
    val type: String,
    val description: String = "",
    val paymentMethod: String = ""
)

// Diálogo para agregar transacción
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: String,
    onDismiss: () -> Unit,
    onAdd: (TransactionInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }

    val categories = listOf(
        "Alimentación", "Transporte", "Entretenimiento",
        "Salud", "Servicios", "Educación", "Ropa", "Otros"
    )
    val paymentMethods = listOf("Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito", "Transferencia")

    val isValid = name.isNotBlank() &&
            amount.isNotBlank() &&
            (amount.toDoubleOrNull() ?: 0.0) > 0 &&
            category.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (type == "income") "Agregar Ingreso" else "Agregar Gasto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Concepto") },
                    placeholder = { Text("Ej: Salario, Compra...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Text("$") }
                )

                // Selector de categoría
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showCategoryDropdown = true }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (category.isEmpty()) "Selecciona categoría" else category,
                                color = if (category.isEmpty()) Color.Gray else Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Desplegar",
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Selector de método de pago
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showPaymentDropdown = true }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (paymentMethod.isEmpty()) "Método de pago" else paymentMethod,
                                color = if (paymentMethod.isEmpty()) Color.Gray else Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Desplegar",
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPaymentDropdown,
                        onDismissRequest = { showPaymentDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    showPaymentDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Agrega una descripción...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (isValid) {
                                val amountValue = amount.toDoubleOrNull() ?: 0.0
                                onAdd(
                                    TransactionInput(
                                        name = name,
                                        amount = amountValue,
                                        category = category,
                                        type = type,
                                        description = description,
                                        paymentMethod = paymentMethod
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "income")
                                Color(0xFF16A34A) else Color(0xFFDC2626)
                        ),
                        enabled = isValid
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HistorialScreenPreview() {
    MaterialTheme {
        HistorialScreen()
    }
}