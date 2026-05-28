package com.example.finanzapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Obtener todas las transacciones del usuario actual
    suspend fun getAllTransactions(): List<FirestoreTransaction> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreTransaction::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Agregar nueva transacción
    suspend fun addTransaction(transaction: FirestoreTransaction): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val transactionWithUser = transaction.copy(userId = userId)
            val docRef = db.collection("transactions")
                .add(transactionWithUser)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar transacción
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            db.collection("transactions")
                .document(transactionId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener estadísticas por categoría
    suspend fun getCategoryStats(): List<CategoryStats> {
        val transactions = getAllTransactions()

        return transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .map { (category, transList) ->
                CategoryStats(
                    name = category,
                    totalAmount = transList.sumOf { it.amount },
                    transactionCount = transList.size
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    // Obtener estadísticas mensuales (últimos 6 meses)
    suspend fun getMonthlyStats(): List<MonthlyStats> {
        val transactions = getAllTransactions()
        val dateFormat = SimpleDateFormat("MMM", Locale("es", "MX"))
        val calendar = Calendar.getInstance()

        // Generar últimos 6 meses
        val months = (0..5).map { monthsAgo ->
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -monthsAgo)
            MonthlyStats(
                month = dateFormat.format(calendar.time)
                    .replaceFirstChar { it.uppercase() },
                year = calendar.get(Calendar.YEAR),
                income = 0.0,
                expenses = 0.0
            )
        }.reversed()

        // Calcular totales por mes
        val mutableMonths = months.toMutableList()
        transactions.forEach { transaction ->
            calendar.time = transaction.date
            val monthName = dateFormat.format(transaction.date)
                .replaceFirstChar { it.uppercase() }
            val year = calendar.get(Calendar.YEAR)

            val index = mutableMonths.indexOfFirst {
                it.month == monthName && it.year == year
            }

            if (index != -1) {
                val monthStat = mutableMonths[index]
                if (transaction.type == "income") {
                    mutableMonths[index] = monthStat.copy(
                        income = monthStat.income + transaction.amount
                    )
                } else {
                    mutableMonths[index] = monthStat.copy(
                        expenses = monthStat.expenses + transaction.amount
                    )
                }
            }
        }

        return mutableMonths
    }
}