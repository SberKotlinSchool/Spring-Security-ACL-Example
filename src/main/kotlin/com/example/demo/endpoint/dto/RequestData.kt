package com.example.demo.endpoint.dto

data class RequestData(
    val name: String,
    val phone: String,
    var public: Boolean?
)
