package com.example.ihryskoscanner

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Details(val name: String, val price: String, val id: String, val image: String, val location: String, val location_shop: String)

class Search {
    fun searchAndRetrieveDetails(targetBarcode: String, xmlContent: String): Details? {
        val document = Jsoup.parse(xmlContent)

        val itemElements = document.select("ITEM")

        for (item in itemElements) {
            val barcodeTag = item.selectFirst("EAN")

            if (barcodeTag != null && barcodeTag.text() == targetBarcode) {
                val name = item.selectFirst("NAME")?.text() ?: "N/A"
                val image = item.selectFirst("IMAGE")?.text() ?: "N/A"
                val price = item.selectFirst("PRICE")?.text() ?: "N/A"
                val id = item.selectFirst("ITEM_ID")?.text() ?: "N/A"
                val location = item.selectFirst("LOC")?.text() ?: "N/A"
                val location_shop = item.selectFirst("LOC_SHOP")?.text() ?: "N/A"

                return Details(name, price, id, image, location, location_shop)
            }
        }

        // Return null if the barcode is not found
        return null
    }

    suspend fun fetchXMLContent(): String = withContext(Dispatchers.IO) {
        val targetUrl = "https://gist.githubusercontent.com/mrtineu/80fed64bf9fbe214fd427cd6d318958d/raw/fc7cc3937d087133cb0e56aaa546ba33181f3782/gistfile1.txt"
        val url = URL(targetUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val xmlContent = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    xmlContent.append(line)
                }

                reader.close()
                return@withContext xmlContent.toString()
            } else {
                println("HTTP Request Failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
        return@withContext ""
    }
}
