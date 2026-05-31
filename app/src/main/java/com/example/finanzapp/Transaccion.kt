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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarTransaccionScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Estados
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }  // ← Agrega esto
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val transactionRepository = remember { TransactionRepository() }

// Mostrar mensajes (ERROR o ÉXITO)
    LaunchedEffect(errorMessage, successMessage) {  // ← Escucha ambos
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            successMessage = null
        }
    }

    val categories = listOf(
        "Alimentación", "Transporte", "Entretenimiento",
        "Salud", "Servicios", "Educación", "Ropa", "Otros"
    )

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = selectedType == "expense",
                    onClick = { selectedType = "expense" },
                    label = {
                        Text(
                            "Gasto",
                            color = if (selectedType == "expense") Color.White else Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDC2626),
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFFFEE2E2),
                        disabledLabelColor = Color(0xFFDC2626)
                    )
                )

                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = selectedType == "income",
                    onClick = { selectedType = "income" },
                    label = {
                        Text(
                            "Ingreso",
                            color = if (selectedType == "income") Color.White else Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
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
                onValueChange = { amount = it },
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

            // 3. MÉTODO DE PAGO
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
                        modifier = Modifier.weight(1f),
                        selected = selectedPaymentMethod == method,
                        onClick = { selectedPaymentMethod = method },
                        label = {
                            Text(method, fontSize = 12.sp)
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
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(category, fontSize = 14.sp)
                            },
                            onClick = {
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
                onValueChange = { description = it },
                placeholder = { Text("Ej: Compra del supermercado") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                minLines = 2
            )


            Button(
                onClick = {
                    if (validateForm(amount, selectedPaymentMethod, selectedCategory)) {
                        isLoading = true
                        scope.launch {
                            saveTransaction(
                                repository = transactionRepository,
                                type = selectedType,
                                amount = amount.toDouble(),
                                paymentMethod = selectedPaymentMethod,
                                category = selectedCategory,
                                description = description,
                                onSuccess = {
                                    isLoading = false
                                    successMessage = "Transacción guardada exitosamente"  // ← NUEVO
                                },
                                onError = { errMsg ->
                                    isLoading = false
                                    errorMessage = errMsg
                                }
                            )
                            // Esperar 1 segundo para mostrar el mensaje y regresar
                            delay(1000)
                            onSaveClick()
                        }
                    } else {
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

// Función para validar el formulario
private fun validateForm(
    amount: String,
    paymentMethod: String,
    category: String
): Boolean {
    val amountValue = amount.toDoubleOrNull()
    return amountValue != null && amountValue > 0 &&
            paymentMethod.isNotEmpty() &&
            category.isNotEmpty()
}

// Función para guardar la transacción en Firestore
private suspend fun saveTransaction(
    repository: TransactionRepository,
    type: String,
    amount: Double,
    paymentMethod: String,
    category: String,
    description: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        // VERIFICAR AUTENTICACIÓN PRIMERO
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("No hay usuario autenticado. Debes iniciar sesión.")
            return
        }

        println("Usuario autenticado: ${currentUser.email} (UID: ${currentUser.uid})")

        // Crear objeto FirestoreTransaction
        val transaction = FirestoreTransaction(
            id = UUID.randomUUID().toString(),
            name = if (type == "income") "Ingreso" else "Gasto",
            amount = amount,
            category = category,
            date = Date(),
            type = type,
            description = description,
            paymentMethod = paymentMethod,
            userId = currentUser.uid  // ← Usar el UID real
        )

        // Guardar en Firestore
        val result = repository.addTransaction(transaction)

        if (result.isSuccess) {
            println("Transacción guardada exitosamente")
            onSuccess()
        } else {
            val error = result.exceptionOrNull()
            println("Error al guardar: ${error?.message}")
            onError(error?.message ?: "Error al guardar la transacción")
        }
    } catch (e: Exception) {
        println("Excepción: ${e.message}")
        onError(e.message ?: "Error inesperado al guardar")
    }
}

@Preview(showSystemUi = true)
@Composable
fun AgregarTransaccionScreenPreview() {
    MaterialTheme {
        AgregarTransaccionScreen()
    }
}