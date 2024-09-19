package com.ozzysimpson.project2

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class OznortsManager {
    private val client: OkHttpClient
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(5, TimeUnit.SECONDS)
        client = builder.build()
    }

    fun getWhois(apiId: String, apiSecret: String, accessKey: String, domain: String): WhoisResponse {
        val whoisJsonAdapter = moshi.adapter(WhoisResponse::class.java)

        // Generate URL to query
        val url = "https://www.oznorts.com/includes/api.php"

        // Add post parameters
        val formBody = FormBody.Builder()
            .add("action", "DomainWhois")
            .add("identifier", apiId)
            .add("secret", apiSecret)
            .add("domain", domain)
            .add("responsetype", "json")
            .add("accesskey", accessKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        try {
            // Execute request
            val response: Response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Was not successful, give empty list as response
                return WhoisResponse("error")
            }

            // Convert JSON response -> whoisResponse object
            val json = whoisJsonAdapter.fromJson(response.body!!.source())

            // Return whois info
            return json ?: WhoisResponse("error")
        }
        catch (e: IOException) {
            Log.e("OznortsManager", "Failed to load whois info. $e")
            return WhoisResponse("error")
        }
    }
}