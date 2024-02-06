package com.example.myapplication.ui


import java.io.Serializable

data class UserDetails(
    val userId: String = "",
    val name: String = "",
    val profession: String = "",
    val mobile: String = "",
    val email: String = ""
) : Serializable

