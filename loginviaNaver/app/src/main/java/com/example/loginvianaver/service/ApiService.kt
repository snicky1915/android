package com.example.loginvianaver.service

import com.example.loginvianaver.modell.Payment
import com.example.loginvianaver.modell.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("/api/auth/register")
    fun registerUser(@Body user: User): Call<String>

    @POST("/api/auth/login")
    fun loginUser(@Body user: User): Call<String>

    @GET("/api/users")
    fun getAllUsers(): Call<List<User>>

    @PUT("/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<User>

    @POST("/api/users")
    fun createUser(@Body user: User): Call<User>

    @POST("api/payments")
    fun createPayment(@Body payment: Payment): Call<Payment>
}