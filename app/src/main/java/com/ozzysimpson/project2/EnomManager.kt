package com.ozzysimpson.project2

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class EnomManager {
    private val client: OkHttpClient
    private val markup = 1.35
    private val roundTo = 0.95

    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(5, TimeUnit.SECONDS)
        client = builder.build()
    }

    fun domainAvailability(sld: String, tld: String): DomainResponse {
        // Generate URL to query
        val url = "https://resellertest.enom.com/interface.asp?UID=resellid&PW=resellpw&SLD=$sld&TLD=$tld&Command=check&responsetype=xml&version=2&includeprice=1"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            // Execute request
            val response: Response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Was not successful, give empty list as response
                return DomainResponse("error", "Failed to load domain info. Check your internet connection and try again.", Domain(sld, tld))
            }

            // Get XML response info
            val responseBody = response.body!!.string()
            val status = if (responseBody.contains("<ErrCount>0</ErrCount>")) "success" else "error"
            val error = if (status == "error") "An error occurred while checking for the availability of your domain. Please try again." else null
            var regPrice = responseBody.substringAfter("<Registration>").substringBefore("</Registration>").toDoubleOrNull()
            regPrice = if (regPrice != null) regPrice * markup else 0.00
            regPrice = ceil(regPrice) - (1 - roundTo)
            regPrice = regPrice.toDouble()
            val available = responseBody.substringAfter("<RRPCode>").substringBefore("</RRPCode") == "210"

            // Return domain info
            return DomainResponse(
                status,
                error,
                Domain(
                    sld,
                    tld,
                    regPrice = regPrice,
                    available = available
                )
            )
        }
        catch (e: IOException) {
            Log.e("EnomManager", "Failed to load domain results. $e")
            return DomainResponse(
                "error",
                "Failed to load domain info. Check your internet connection and try again.",
                Domain(sld, tld)
            )
        }
    }
}
