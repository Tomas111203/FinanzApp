package com.example.finanzapp

import java.util.*

object CreditCardHelper {

    // Calcular el pago mensual de una compra
    fun calculateMonthlyPayment(totalAmount: Double, months: Int): Double {
        if (months <= 0) return totalAmount
        return totalAmount / months
    }

    // Calcular deuda pendiente
    fun calculateRemainingDebt(totalAmount: Double, paidSoFar: Double): Double {
        return maxOf(0.0, totalAmount - paidSoFar)
    }

    // Verificar si una transacción de TC está completamente pagada
    fun isFullyPaid(transaction: FirestoreTransaction): Boolean {
        return transaction.creditPaidSoFar >= transaction.amount
    }

    // Obtener el siguiente pago programado (para mostrar al usuario)
    fun getNextPaymentAmount(transaction: FirestoreTransaction): Double {
        if (isFullyPaid(transaction)) return 0.0
        val monthlyPayment = calculateMonthlyPayment(transaction.amount, transaction.creditInstallments)
        val remaining = calculateRemainingDebt(transaction.amount, transaction.creditPaidSoFar)
        return minOf(monthlyPayment, remaining)
    }
}