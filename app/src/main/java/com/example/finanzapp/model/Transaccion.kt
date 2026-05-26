package com.example.finanzapp.model

import com.google.firebase.Timestamp

data class Transaccion(
    val id: String = "",  // ID automático de Firestore
    val descripcion: String = "",
    val monto: Double = 0.0,
    val tipo: String = "",  // "ingreso" o "gasto"
    val categoria: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val usuarioId: String = ""  // UID del usuario que creó la transacción
)
