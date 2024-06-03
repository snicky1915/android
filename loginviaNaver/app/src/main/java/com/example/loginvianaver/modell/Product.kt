package com.example.loginvianaver.modell

data class Product(
    val id: Long?,
    val name: String?,
    val description: String?,
    val price: Double?,
    val quantity: Int?,
    val user: User?
)