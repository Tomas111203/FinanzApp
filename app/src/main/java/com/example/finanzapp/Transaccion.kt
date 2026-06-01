package com.example.finanzapp

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarTransaccionScreen(
    viewModel: TransactionViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    println("[Transaccion] Inicializando pantalla de Agregar Transacción")

    var selectedType by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Estados
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mostrar mensajes (ERROR o ÉXITO)
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            println("[Transaccion] Mostrando error: $it")
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
        successMessage?.let {
            println("[Transaccion] Mostrando éxito: $it")
            snackbarHostState.showSnackbar(it)
            successMessage = null
        }
    }

    val categoriesGasto = listOf(
        "Alimentación", "Transporte", "Entretenimiento",
        "Salud", "Servicios", "Educación", "Ropa", "Otros"
    )
    val categoriesIngreso = listOf(
        "Sueldo","Inversión","Freelance","Herencia","Regalo","Venta","Otro"
    )

    var currentCategories: List<String> = categoriesGasto  // Valor inicial por defecto


    val paymentMethods = listOf("Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito")

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
                        onClick = {
                            println("[Transaccion] Click en botón VOLVER")
                            onBackClick()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Agregar transacción",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 1. TIPO DE TRANSACCIÓN
            Text(
                text = "Tipo de transacción",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilterChip(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    selected = selectedType == "expense",
                    onClick = {
                        println("[Transaccion] Seleccionado: GASTO")
                        selectedType = "expense"
                    },
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Gasto",
                                color = if (selectedType == "expense") Color.White else Color(
                                    0xFFDC2626
                                ),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDC2626),
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFFEE2E2),
                        disabledLabelColor = Color(0xFFDC2626)
                    )
                )

                FilterChip(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    selected = selectedType == "income",
                    onClick = {
                        println("[Transaccion] Seleccionado: INGRESO")
                        selectedType = "income"
                        //  Limpiar método de pago al cambiar a ingreso
                        selectedPaymentMethod = ""
                    },
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ingreso",
                                color = if (selectedType == "income") Color.White else Color(
                                    0xFF16A34A
                                ),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF16A34A),
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFDCFCE7),
                        disabledLabelColor = Color(0xFF16A34A)
                    )
                )
            }

            // 2. MONTO
            Text(
                text = "Monto",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    println("[Transaccion] Monto cambiado: $it")
                },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("$", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = amount.isNotEmpty() && amount.toDoubleOrNull() == null
            )

            if (amount.isNotEmpty() && amount.toDoubleOrNull() == null) {
                Text(
                    text = "Ingresa un monto válido",
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626)
                )
            }

            // 3. METODO DE PAGO - SOLO PARA GASTOS
            if (selectedType == "expense") {
                Text(
                    text = "Método de pago",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                paymentMethods.forEach { method ->
                    FilterChip(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),  // ← Altura fija, el contenido se adaptará
                        selected = selectedPaymentMethod == method,
                        onClick = {
                            println("[Transaccion] Método de pago seleccionado: $method")
                            selectedPaymentMethod = method
                        },
                        label = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),  // ← Ocupa todo el espacio del FilterChip
                                contentAlignment = Alignment.Center  // ← Centra TODO el contenido (icono + texto)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.wrapContentSize()  // ← El contenido ocupa solo lo necesario
                                ) {
                                    // Icono (arriba)
                                    Icon(
                                        painter = painterResource(
                                            when (method) {
                                                "Efectivo" -> R.drawable.lucide__banknote
                                                "Tarjeta de Débito" -> R.drawable.lucide__credit_card
                                                else -> R.drawable.lucide__credit_card
                                            }
                                        ),
                                        contentDescription = method,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (selectedPaymentMethod == method) Color.White else Color(0xFF3B82F6)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Texto (debajo)
                                    Text(
                                        method,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White,
                            disabledContainerColor = Color(0xFFEFF6FF),
                            disabledLabelColor = Color(0xFF3B82F6)
                        )
                    )
                }
            }

                if (selectedPaymentMethod.isEmpty()) {
                    Text(
                        text = "* Selecciona un método de pago",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            // 3.5 MESES PARA TARJETA DE CRÉDITO (solo si se seleccionó TC)
            // 3.5 MESES PARA TARJETA DE CRÉDITO (solo si se seleccionó TC)
            var selectedInstallments by remember { mutableStateOf(1) }
            var showInstallmentsError by remember { mutableStateOf(false) }
            var customMonths by remember { mutableStateOf("") }

            if (selectedType == "expense" && selectedPaymentMethod == "Tarjeta de Crédito") {
                // Box para agrupar y controlar el espaciado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp)  // Sin padding superior para reducir espacio
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  // Espacio reducido entre elementos
                    ) {
                        // Título
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.lucide__credit_card),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF3B82F6)
                            )
                            Text(
                                text = "Plazo de pago",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }

                        // Fila 1: Contado y 3 meses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedInstallments == 1,
                                onClick = {
                                    selectedInstallments = 1
                                    customMonths = ""
                                    showInstallmentsError = false
                                },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ){
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (selectedInstallments == 1) Color.White else Color(
                                                    0xFF3B82F6
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Contado", fontSize = 13.sp)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = Color(0xFFEFF6FF),
                                    disabledLabelColor = Color(0xFF3B82F6)
                                )
                            )

                            FilterChip(
                                selected = selectedInstallments == 3,
                                onClick = {
                                    selectedInstallments = 3
                                    customMonths = ""
                                    showInstallmentsError = false
                                },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "3",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("meses", fontSize = 10.sp)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = Color(0xFFEFF6FF),
                                    disabledLabelColor = Color(0xFF3B82F6)
                                )
                            )
                        }

                        // Fila 2: 6 y 12 meses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedInstallments == 6,
                                onClick = {
                                    selectedInstallments = 6
                                    customMonths = ""
                                    showInstallmentsError = false
                                },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "6",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("meses", fontSize = 10.sp)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = Color(0xFFEFF6FF),
                                    disabledLabelColor = Color(0xFF3B82F6)
                                )
                            )

                            FilterChip(
                                selected = selectedInstallments == 12,
                                onClick = {
                                    selectedInstallments = 12
                                    customMonths = ""
                                    showInstallmentsError = false
                                },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "12",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("meses", fontSize = 10.sp)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = Color(0xFFEFF6FF),
                                    disabledLabelColor = Color(0xFF3B82F6)
                                )
                            )
                        }

                        // Separador "o" - más compacto
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "o",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Campo para meses personalizados
                        OutlinedTextField(
                            value = customMonths,
                            onValueChange = {
                                customMonths = it
                                val value = it.toIntOrNull()
                                if (value != null && value in 1..36) {
                                    selectedInstallments = value
                                    showInstallmentsError = false
                                } else if (it.isNotEmpty()) {
                                    showInstallmentsError = true
                                } else {
                                    selectedInstallments = 1
                                    showInstallmentsError = false
                                }
                            },
                            placeholder = {
                                Text(
                                    "Escribe otros meses (ej: 9, 18, 24)",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF3B82F6)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showInstallmentsError,
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.LightGray,
                                errorBorderColor = Color(0xFFDC2626)
                            ),
                            supportingText = {
                                if (showInstallmentsError) {
                                    Text(
                                        "Ingresa un número entre 1 y 36 meses",
                                        fontSize = 10.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                } else if (customMonths.isEmpty() && selectedInstallments <= 12) {
                                    Text(
                                        "Selecciona un plazo o ingresa uno personalizado",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        )

                        // Mostrar mensualidad estimada
                        if (selectedInstallments > 0 && amount.toDoubleOrNull() != null) {
                            val monto = amount.toDoubleOrNull() ?: 0.0
                            val mensualidad = monto / selectedInstallments
                            if (monto > 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF0F9FF)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💳 Mensualidad estimada:",
                                            fontSize = 11.sp,
                                            color = Color(0xFF3B82F6)
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", mensualidad)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. CATEGORÍA
            Text(
                text = "Categoría",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            currentCategories = if (selectedType == "expense") {
                                categoriesGasto
                            } else {
                                categoriesIngreso
                            }
                            println("[Transaccion] Abriendo dropdown de categorías")
                            showCategoryDropdown = true
                        }
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
                            text = if (selectedCategory.isEmpty()) "Selecciona una categoría" else selectedCategory,
                            color = if (selectedCategory.isEmpty()) Color.Gray else Color.Black,
                            fontSize = 14.sp
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
                    onDismissRequest = {
                        showCategoryDropdown = false
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    currentCategories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(category, fontSize = 14.sp)
                            },
                            onClick = {
                                println("[Transaccion] Categoría seleccionada: $category")
                                selectedCategory = category
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            if (selectedCategory.isEmpty()) {
                Text(
                    text = "* Selecciona una categoría",
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626)
                )
            }

            // 5. DESCRIPCIÓN
            Text(
                text = "Descripción (opcional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    println("[Transaccion] Descripción cambiada: $it")
                },
                placeholder = { Text("Ej: Compra del supermercado") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                minLines = 2
            )

            Button(
                onClick = {
                    println("==========================================")
                    println("[Transaccion] Click en botón GUARDAR")
                    println("   Tipo: $selectedType")
                    println("   Monto: $amount")
                    println("   Método pago: $selectedPaymentMethod")
                    println("   Categoría: $selectedCategory")
                    println("   Descripción: $description")
                    println("==========================================")

                    //  Validar según el tipo de transacción
                    val isValid = if (selectedType == "expense") {
                        validateForm(amount, selectedPaymentMethod, selectedCategory)
                    } else {
                        validateIncomeForm(amount, selectedCategory)
                    }

                    if (isValid) {
                        println("[Transaccion] Formulario válido")
                        isLoading = true
                        println("[Transaccion] isLoading = true")

                        scope.launch {
                            println("[Transaccion] Llamando a viewModel.saveTransaction()...")
                            val result = viewModel.saveTransaction(
                                type = selectedType,
                                amount = amount.toDouble(),
                                paymentMethod = if (selectedType == "expense") selectedPaymentMethod else "",
                                category = selectedCategory,
                                description = description,
                                creditInstallments = if (selectedPaymentMethod == "Tarjeta de Crédito") selectedInstallments else 1 // NUEVO
                            )

                            isLoading = false
                            println("[Transaccion] isLoading = false")

                            if (result.isSuccess) {
                                println("[Transaccion] Transacción guardada con éxito!")
                                successMessage = " Transacción guardada exitosamente"
                                delay(1500)
                                println(" [Transaccion] Llamando a onSaveClick()...")
                                onSaveClick()
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Error al guardar"
                                println(" [Transaccion] Error: $error")
                                errorMessage = error
                            }
                        }
                    } else {
                        println(" [Transaccion] Formulario INVALIDO")
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor completa todos los campos obligatorios")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "income") Color(0xFF16A34A) else Color(0xFFDC2626)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar transacción",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Función para validar el formulario de GASTO
private fun validateForm(
    amount: String,
    paymentMethod: String,
    category: String
): Boolean {
    val amountValue = amount.toDoubleOrNull()
    val isValid = amountValue != null && amountValue > 0 &&
            paymentMethod.isNotEmpty() &&
            category.isNotEmpty()

    println(" [Transaccion] Validando formulario de GASTO...")
    println("   - amountValue: $amountValue")
    println("   - paymentMethod: $paymentMethod")
    println("   - category: $category")
    println("   - isValid: $isValid")

    return isValid
}

//  Nueva función para validar el formulario de INGRESO (sin método de pago)
private fun validateIncomeForm(
    amount: String,
    category: String
): Boolean {
    val amountValue = amount.toDoubleOrNull()
    val isValid = amountValue != null && amountValue > 0 &&
            category.isNotEmpty()

    println(" [Transaccion] Validando formulario de INGRESO...")
    println("   - amountValue: $amountValue")
    println("   - category: $category")
    println("   - isValid: $isValid")

    return isValid
}

@Preview(showSystemUi = true)
@Composable
fun AgregarTransaccionScreenPreview() {
    MaterialTheme {
        AgregarTransaccionScreen()
    }
}