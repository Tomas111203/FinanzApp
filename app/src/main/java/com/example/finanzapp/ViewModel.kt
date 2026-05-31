package com.example.finanzapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        description: String
    ): Result<String> {
        println("[ViewModel] Intentando guardar transacción...")
        println("   - Tipo: $type")
        println("   - Monto: $amount")
        println("   - Categoría: $category")
        println("   - Método de pago: $paymentMethod")

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
            userId = currentUser.uid
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
}