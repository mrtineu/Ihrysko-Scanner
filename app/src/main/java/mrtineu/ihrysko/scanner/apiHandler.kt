package mrtineu.ihrysko.scanner

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
// Ensure Product is imported if not in the same file, e.g.
// import mrtineu.ihrysko.scanner.Product

// Ktor HttpClient instance
private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

// Json decoder instance
private val jsonDecoder = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

private val API_KEY_PREFERENCE = stringPreferencesKey("api_key")

suspend fun getApiKey(context: Context): String {
    val apiKey: String? = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY_PREFERENCE]
        }
        .firstOrNull()
    return apiKey ?: ""
}

// Change return type to List<Product>
suspend fun getByEAN(context: Context, ean: String): List<Product> {
    val apiKey: String = getApiKey(context)

    if (apiKey.isBlank()) {
        println("API key is missing or blank. Cannot fetch product for EAN $ean.")
        return emptyList() // Return empty list if API key is missing
    }

    val requestUrl = "${AppConfig.API_BASE_URL}search/barcode/$ean"
    println("Requesting URL: $requestUrl with API Key: $apiKey")


    return try {
        val response: HttpResponse = client.get(requestUrl) {
            header("auth", apiKey)
        }

        if (response.status.isSuccess()) {
            val responseBodyText = response.bodyAsText()

            if (responseBodyText.isBlank()) {
                println("API returned an empty successful response for EAN $ean.")
                return emptyList()
            }
            println("Raw JSON response for EAN $ean: $responseBodyText")

            val products = jsonDecoder.decodeFromString<List<Product>>(responseBodyText)
            // Log received products
            println("Received products for EAN $ean:")
            products.forEachIndexed { index, product ->
                println("   Product ${index + 1}: $product")
            }
            products // Return the full list of products
        } else {
            println("API Error for EAN $ean: ${response.status} - ${response.bodyAsText()}")
            emptyList() // Return empty list on API error
        }
    } catch (e: kotlinx.serialization.SerializationException) {
        println("JSON Deserialization failed for EAN $ean: ${e.localizedMessage}")
        e.printStackTrace()
        emptyList() // Return empty list on deserialization error
    } catch (e: Exception) {
        println("Network request or other error for EAN $ean: ${e.localizedMessage}")
        e.printStackTrace()
        emptyList() // Return empty list on other errors
    }
}