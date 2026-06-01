package com.example.finanzapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class TransactionViewModel : ViewModel() {
    private val repository = TransactionRepository()

    // Estados observables
    private val _transactions = MutableStateFlow<List<FirestoreTransaction>>(emptyList())
    val transactions: StateFlow<List<FirestoreTransaction>> = _transactions.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStats>> = _categoryStats.asStateFlow()

    private val _monthlyStats = MutableStateFlow<List<MonthlyStats>>(emptyList())
    val monthlyStats: StateFlow<List<MonthlyStats>> = _monthlyStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Cargar todas las transacciones
    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            println("[ViewModel] Iniciando carga de transacciones...")
            try {
                val transactions = repository.getAllTransactions()
                println("[ViewModel] Transacciones obtenidas: ${transactions.size}")
                _transactions.value = transactions
                _categoryStats.value = repository.getCategoryStats()
                _monthlyStats.value = repository.getMonthlyStats()
                _errorMessage.value = null
            } catch (e: Exception) {
                println("[ViewModel] Error: ${e.message}")
                _errorMessage.value = e.message ?: "Error al cargar datos"
                // En caso de error, mantener los datos anteriores
            } finally {
                _isLoading.value = false
                println("[ViewModel] Carga finalizada, isLoading: false")
            }
        }
    }

    suspend fun saveTransaction(
        type: String,
        amount: Double,
        paymentMethod: String,
        category: String,
        description: String,
        creditInstallments: Int = 1
    ): Result<String> {
        println("[ViewModel] Intentando guardar transacción...")
        println("   - Tipo: $type")
        println("   - Monto: $amount")
        println("   - Categoría: $category")
        println("   - Método de pago: $paymentMethod")
        println("   - Meses: $creditInstallments") // NUEVO

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            println("[ViewModel] Usuario no autenticado")
            return Result.failure(Exception("Usuario no autenticado"))
        }

        println("[ViewModel] Usuario autenticado: ${currentUser.email}")

        val transaction = FirestoreTransaction(
            id = UUID.randomUUID().toString(),
            name = if (type == "income") "Ingreso" else "Gasto",
            amount = amount,
            category = category,
            date = Date(),
            type = type,
            description = description,
            paymentMethod = paymentMethod,
            userId = currentUser.uid,
                    // NUEVOS CAMPOS
            creditInstallments = creditInstallments,
            creditPaidSoFar = 0.0,
            originalAmount = if (paymentMethod == "Tarjeta de Crédito") amount else 0.0
        )

        println("[ViewModel] Transacción a guardar: ${transaction}")

        val result = repository.addTransaction(transaction)

        if (result.isSuccess) {
            println("[ViewModel] Transacción guardada exitosamente. ID: ${result.getOrNull()}")
            println("[ViewModel] Recargando datos...")
            loadTransactions()
        } else {
            println("[ViewModel] Error al guardar: ${result.exceptionOrNull()?.message}")
        }

        return result
    }

    suspend fun registerCreditCardPayment(transactionId: String, amount: Double): Result<Unit> {
        return try {
            val transactions = _transactions.value
            val transaction = transactions.find { it.id == transactionId }

            if (transaction != null) {
                val newPaidSoFar = transaction.creditPaidSoFar + amount

                // 1. Actualizar la deuda en la transacción original
                val db = FirebaseFirestore.getInstance()
                db.collection("transactions")
                    .document(transactionId)
                    .update(
                        mapOf(
                            "creditPaidSoFar" to newPaidSoFar
                        )
                    )
                    .await()

                // 2. REGISTRAR COMO TRANSFERENCIA (NO como gasto)
                // Esto es crucial: el pago reduce tu saldo bancario pero NO es un gasto nuevo
                val paymentTransfer = FirestoreTransaction(
                    id = UUID.randomUUID().toString(),
                    name = "Pago Tarjeta",
                    amount = amount,
                    category = "Transferencia",  // ← Nueva categoría especial
                    date = Date(),
                    type = "transfer",  // ← NUEVO tipo "transfer" en lugar de "expense"
                    description = "Pago de tarjeta: ${transaction.category}",
                    paymentMethod = "Transferencia",
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    creditInstallments = 1,
                    creditPaidSoFar = amount,
                    originalAmount = amount
                )

                repository.addTransaction(paymentTransfer)

                // 3. Recargar datos
                loadTransactions()

                Result.success(Unit)
            } else {
                Result.failure(Exception("Transacción no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}