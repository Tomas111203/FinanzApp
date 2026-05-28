package com.example.finanzapp

import java.util.Date

// Modelo principal para Firestore
data class FirestoreTransaction(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Date = Date(),
    val type: String = "", // "income" o "expense"
    val description: String = "",
    val paymentMethod: String = "",
    val userId: String = ""
)

// Modelo para estadísticas por categoría
data class CategoryStats(
    val name: String,
    val totalAmount: Double,
    val transactionCount: Int
)

// Modelo para estadísticas mensuales
data class MonthlyStats(
    val month: String,
    val year: Int,
    val income: Double,
    val expenses: Double
)