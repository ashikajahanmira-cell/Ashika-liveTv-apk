package com.ashika.livetv

data class Channel(
    val name: String,
    val url: String,
    val logo: String = "",
    val group: String = "",
    val country: String = ""
)
