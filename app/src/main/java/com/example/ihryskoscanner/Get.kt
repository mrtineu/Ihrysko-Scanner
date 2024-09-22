package com.example.ihryskoscanner


import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import kotlinx.serialization.Serializable
import com.google.gson.Gson


@Serializable data class Details(val name: String?=null, val image: String?=null, val price: String?=null, val item_id: String?=null,  val loc: String?=null, val loc_shop: String?=null, val stock: String?=null)
class Get {

    companion object {
        // Making authToken a public static variable in the companion object
        var authToken: String? = null   
    }
    fun ean(barcode: String): List<Details>? {
        val url = URL("https://eshop-w10.tail90e65e.ts.net/search/barcode/$barcode")


        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            setRequestProperty("auth", authToken)

            val responseCode = responseCode

            Log.i("INFO", "Response code ${responseCode}")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream.bufferedReader().use { reader ->
                    val responseBody = reader.readText()
                    Log.i("INFO","$responseBody")
                    val gson = Gson()

                    return gson.fromJson(responseBody, Array<Details>::class.java).toList()
                }
            } else {
                return null
            }
        }
    }
    fun name(namee: String): List<Details>? {
        val url = URL("https://eshop-w10.tail90e65e.ts.net/search/name/$namee")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            setRequestProperty("auth", authToken)

            val responseCode = responseCode

            Log.i("INFO", "Response code ${responseCode}")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream.bufferedReader().use { reader ->
                    val responseBody = reader.readText()
                    Log.i("INFO","$responseBody")
                    val gson = Gson()
                    Log.d("INFO",gson.fromJson(responseBody, Array<Details>::class.java).toList().toString())
                    return gson.fromJson(responseBody, Array<Details>::class.java).toList()
                }
            } else {
                return null
            }
        }
    }
}