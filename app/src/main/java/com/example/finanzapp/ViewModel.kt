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
            try {
                _transactions.value = repository.getAllTransactions()
                _categoryStats.value = repository.getCategoryStats()
                _monthlyStats.value = repository.getMonthlyStats()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar datos"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Guardar nueva transacción
    suspend fun saveTransaction(
        type: String,
        amount: Double,
        paymentMethod: String,
        category: String,
        description: String
    ): Result<String> {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }

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

        val result = repository.addTransaction(transaction)

        if (result.isSuccess) {
            // Recargar datos después de guardar
            loadTransactions()
        }

        return result
    }

    // Eliminar transacción
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val result = repository.deleteTransaction(transactionId)

        if (result.isSuccess) {
            // Recargar datos después de eliminar
            loadTransactions()
        }

        return result
    }
}