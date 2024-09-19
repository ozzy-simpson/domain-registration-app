package com.ozzysimpson.project2

data class Domain(
    val sld: String,
    val tld: String,
    val registration: Long ?= null,
    val expiration: Long ?= null,
    var regPrice: Double ?= 0.00,
    val id: String ?= null,
    var available: Boolean ?= false
)

data class DomainResponse(
    val status: String,
    val error: String ?= null,
    val domain: Domain
)
