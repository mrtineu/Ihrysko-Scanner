package com.example.ihryskoscanner

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import android.graphics.Bitmap
import android.view.View
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : Activity(), CoroutineScope {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
        .build()
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var changeTextButton: Button
    private lateinit var itemContainer: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var idTextView: TextView
    private lateinit var itemImageView: ImageView
    private lateinit var syncView: TextView
    private lateinit var eanView: TextView
    private lateinit var locView: TextView
    private lateinit var locshopView: TextView
    private lateinit var stockView: TextView
    private val search = Get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job = Job()

        changeTextButton = findViewById(R.id.imageButton)
        itemContainer = findViewById(R.id.itemContainer)
        nameTextView = findViewById(R.id.nameTextView)
        priceTextView = findViewById(R.id.priceTextView)
        idTextView = findViewById(R.id.idTextView)
        itemImageView = findViewById(R.id.itemImageView)
        syncView = findViewById(R.id.syncview)
        eanView = findViewById(R.id.eanTextView)
        locView = findViewById(R.id.VivoLocation)
        locshopView = findViewById(R.id.VivoLocationShop)
        stockView = findViewById(R.id.VivoStock)

        Log.d("INFO", "Initialized views")

        changeTextButton.setOnClickListener {
            launch {
                changeText()
            }
        }
        changeTextButton.visibility = View.VISIBLE
    }

    private fun changeText() {
        val scanner = GmsBarcodeScanning.getClient(this, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                launch {
                    val barcodeValue = barcode.rawValue.toString()
                    Log.d("INFO", "Barcode scanned $barcodeValue")

                    withContext(Dispatchers.Main) {
                        processXMLContent(barcode)
                    }
                }
            }
            .addOnFailureListener { e ->
                itemContainer.removeAllViews()
                Log.d("ERROR", e.stackTraceToString())
            }
    }

    private suspend fun loadImageFromNetwork(imageUrl: String?, imageView: ImageView) {
        withContext(Dispatchers.Main) {


            Picasso.get()
                .load(imageUrl)

                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        Log.d("INFO", "Image loaded successfully")
                    }

                    override fun onError(e: Exception?) {
                        Log.e("ERROR", "Error loading image: ${e?.message}")
                    }
                })
        }
    }

    private suspend fun processXMLContent(barcode: Barcode) {
        val start = System.currentTimeMillis()
        Log.d("INFO", "XML search started ")

        syncView.text = "Started search"

        try {
            val details = withContext(Dispatchers.IO) {
                search.ean(barcode.rawValue.toString())
            }

            details?.let { list ->
                if (list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val detail = list.first()
                        nameTextView.text = "Name: ${detail.name}"
                        priceTextView.text = "Price: ${detail.price}"
                        idTextView.text = "ID: ${detail.item_id}"
                        eanView.text = "EAN: ${barcode.rawValue.toString()}"
                        locView.text = "Sklady: ${detail.location}"
                        locshopView.text = "Shop: ${detail.location_shop}"
                        stockView.text = "Stock: ${detail.stock}"

                        launch {
                            try {
                                val imageUrl = detail.image
                                loadImageFromNetwork(imageUrl, itemImageView)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        changeTextButton.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        nameTextView.text = "Name: Barcode not found"
                        eanView.text = "EAN: ${barcode.rawValue.toString()}"
                        locView.text = ""
                        locshopView.text = ""
                        idTextView.text = ""
                        priceTextView.text = ""
                        stockView.text = ""
                        Log.d("WARNING", "Barcode not found")
                        changeTextButton.visibility = View.VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Handle exception
                e.printStackTrace()
                Log.e("ERROR", "Error fetching details: ${e.message}")
                syncView.text = "Error: ${e.message}"
            }
        }

        val time = System.currentTimeMillis() - start
        syncView.text = "Finished in $time ms"
        Log.d("INFO", "Rendering finished in $time")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}