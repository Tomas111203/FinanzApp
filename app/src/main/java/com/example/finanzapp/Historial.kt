package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

// Modelo de datos
data class Transaction(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String // "income" o "expense"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBackClick: () -> Unit = {}
) {
    // Datos mock
    val mockTransactions = remember {
        listOf(
            Transaction(1, "Supermercado", -450.5, "Alimentación", "28 Mar 2026", "expense"),
            Transaction(2, "Salario", 18000.0, "Ingreso", "25 Mar 2026", "income"),
            Transaction(3, "Gasolina", -280.0, "Transporte", "27 Mar 2026", "expense"),
            Transaction(4, "Netflix", -129.0, "Entretenimiento", "26 Mar 2026", "expense"),
            Transaction(5, "Farmacia", -320.5, "Salud", "24 Mar 2026", "expense"),
            Transaction(6, "Uber", -85.0, "Transporte", "23 Mar 2026", "expense"),
            Transaction(7, "Restaurante", -650.0, "Alimentación", "22 Mar 2026", "expense"),
            Transaction(8, "Freelance", 2500.0, "Ingreso", "20 Mar 2026", "income")
        )
    }

    val categories = listOf(
        "Todas las categorías",
        "Alimentación",
        "Transporte",
        "Entretenimiento",
        "Salud",
        "Servicios",
        "Ingreso"
    )

    // Estados
    var searchTerm by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas las categorías") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Filtrar transacciones
    val filteredTransactions = mockTransactions.filter { transaction ->
        val matchesSearch = transaction.name.lowercase().contains(searchTerm.lowercase())
        val matchesCategory = selectedCategory == "Todas las categorías" ||
                transaction.category == selectedCategory
        matchesSearch && matchesCategory
    }

    // Calcular balance total
    val totalBalance = filteredTransactions.sumOf { it.amount }

    // Formateador de moneda
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "MX")) }

    Scaffold(
        topBar = {
            // Header
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
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF0FDF4) // Gradiente suave verde
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Sección de búsqueda y filtro
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                // Filtro por categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter),
                        contentDescription = "Filtrar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Box {
                        // Botón que muestra el dropdown
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

                        // Dropdown menu
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White, RoundedCornerShape(8.dp))
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de transacciones
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron transacciones",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTransactions) { transaction ->
                            TransactionItem(transaction, currencyFormatter)
                            if (transaction != filteredTransactions.last()) {
                                Divider(
                                    color = Color(0xFFE5E7EB),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total de transacciones:",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = filteredTransactions.size.toString(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Balance del período:",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$${currencyFormatter.format(totalBalance)}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = if (totalBalance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = transaction.category,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = transaction.date,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            val amountText = if (transaction.amount >= 0) {
                "+$${currencyFormatter.format(transaction.amount)}"
            } else {
                "-$${currencyFormatter.format(kotlin.math.abs(transaction.amount))}"
            }

            Text(
                text = amountText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (transaction.amount >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
            )
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