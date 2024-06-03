package com.example.loginvianaver.modell

data class User(
    val id: Int?,
    val username: String?,
    val email: String?,
    val lat: Double?,
    val lng: Double?,
    val phone: String?,
    val password: String?,
    val inventoryQuantity : String?
)