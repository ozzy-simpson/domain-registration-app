package com.ozzysimpson.project2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhoisResponse(
    var result: String,
    var whois: String ?= null
)
