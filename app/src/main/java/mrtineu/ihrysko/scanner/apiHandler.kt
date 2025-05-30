package mrtineu.ihrysko.scanner

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body // Added explicit import
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull // Import for firstOrNull
import kotlinx.coroutines.flow.map // Import for map operator
import kotlinx.serialization.json.Json

private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true // Important if API sends more fields than in your data class
        })
    }
}
private val API_KEY_PREFERENCE = stringPreferencesKey("api_key")
suspend fun getApiKey(context: Context): String {
    val apiKey: String? = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY_PREFERENCE] // It will be null if not found
        }
        .firstOrNull() // Get the first emitted value or null if the flow is empty
    return apiKey ?: "" // Simplified return, handles null by returning empty string
}
suspend fun getByEAN(context: Context, ean: String): Product? {
    val apiKey: String = getApiKey(context)

    if (apiKey.isBlank()) { // Check if API key is blank (empty or whitespace)
        println("API key is missing or blank. Cannot fetch product for EAN $ean.")
        return null // Return null if API key is not valid
    }

    return try {
        val response: HttpResponse = client.get(AppConfig.API_BASE_URL+"/search/$ean") {
            parameter("auth", apiKey)
        }

        if (response.status.isSuccess()) {
            val products = response.body<List<Product>>() // Ktor deserializes JSON array to List<Product>
            products.firstOrNull() // Return the first product if the list is not empty, otherwise null
        } else {
            println("API Error: ${response.status} - ${response.bodyAsText()}")
            null
        }
    } catch (e: Exception) {
        println("Network request failed for EAN $ean: ${e.localizedMessage}")
        e.printStackTrace() // Good for debugging
        null

    }
}
