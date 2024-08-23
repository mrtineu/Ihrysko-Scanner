package com.example.ihryskoscanner


import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import kotlinx.serialization.Serializable
import com.google.gson.Gson


@Serializable data class Details(val name: String?=null, val image: String?=null, val price: String?=null, val item_id: String?=null,  val location: String?=null, val location_shop: String?=null, val stock: String?=null)
class Get {


    fun ean(barcode: String): List<Details>? {
        val url = URL("http://192.168.100.21:8000/search/barcode/$barcode")
        val authToken = "SAsgv.gIwlN1PS8IW8SWEYmNErqllMXSClPLi8yGNZPWGi9FZIfqmte8mtopO4xjV2tb9Emv5vZqT5eijwxyDawpvLezxFsJDCR2VCsLQa29MtMouaImYOmH1xegz5Tn4iBbVe"

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
}