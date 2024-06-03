package com.example.loginvianaver.modell



data class Payment(
    val paymentId: Int? = null,
    val user: User,
    val paymentName: String
)
