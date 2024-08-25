package com.example.ihryskoscanner

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
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
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : Activity(), CoroutineScope {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
        .build()
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var changeTextButton: Button
    private lateinit var itemContainer: ConstraintLayout
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
    private lateinit var searchView: SearchView
    private lateinit var recyclerViewSuggestions: RecyclerView
    private val search = Get()
    private lateinit var adapter: ProductAdapter
    private lateinit var settings: ImageButton

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
        eanView = findViewById(R.id.eanTextView)
        locView = findViewById(R.id.VivoLocation)
        locshopView = findViewById(R.id.VivoLocationShop)
        stockView = findViewById(R.id.VivoStock)
        searchView = findViewById(R.id.searchView)
        settings = findViewById(R.id.imageButton2)
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions)
        LoadKey()
        if (Get.authToken == null) {
            nameTextView.text = "no API key is specified go into settings to change it"
        }
        // Initialize the RecyclerView (assuming you have set up an adapter and layout manager)
        recyclerViewSuggestions.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(mutableListOf()) { product ->
            // Handle item click here
            searchView.clearFocus() // Close the search view
            recyclerViewSuggestions.visibility = View.GONE
            Log.d("INFO",product.toString())
            val pricen: String? = product.price
            val priced: Double = pricen?.toDouble()?:0.0
            idTextView.text = "ID: ${product.item_id}"


            nameTextView.text = "Name: ${product.name}"
            priceTextView.text = "Price: ${(priced*1.2)}"
            locView.text = "Sklady: ${product.loc}"
            locshopView.text = "Shop: ${product.loc_shop}"
            stockView.text = "Stock: ${product.stock}"

            // Optionally, load the image if needed
            launch {
                try {
                    val imageUrl = product.image
                    loadImageFromNetwork(imageUrl, findViewById(R.id.itemImageView))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        recyclerViewSuggestions.adapter = adapter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!TextUtils.isEmpty(newText)) {
                    fetchSuggestions(newText.toString())
                } else {
                    recyclerViewSuggestions.visibility = View.GONE
                }
                return false
            }
        })
        Log.d("INFO", "Initialized views")

        changeTextButton.setOnClickListener {
            launch {
                changeText()
            }
        }
        changeTextButton.visibility = View.VISIBLE

        settings.setOnClickListener {
            val dialog = ApiKeyDialog(this)

            // Setting up the submit listener
            dialog.setOnSubmitListener { apiKey ->
                // Handle the API key submission here
                println("API Key submitted: $apiKey")
                SaveKey(apiKey)
                LoadKey()
            }

            // Setting up the cancel listener
            dialog.setOnCancelListener {
                // Handle the cancel action here
                println("Dialog cancelled")
            }

            dialog.show()
        }
    }



        private fun fetchSuggestions(query: String) {
            launch {
                val details = withContext(Dispatchers.IO) {
                        search.name(query)

                }
                if (details != null) {
                    adapter.updateList(details)
                    recyclerViewSuggestions.visibility = View.VISIBLE
                } else {
                    recyclerViewSuggestions.visibility = View.GONE
                    Log.e("ERROR", "No suggestions found")
                }
            }
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

        private fun SaveKey(key: String) {
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("apiKey", key)
            editor.apply()
        }

        private fun LoadKey() {
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val apiKey = sharedPreferences.getString("apiKey", null)
            Get.authToken = apiKey
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
                            locView.text = "Sklady: ${detail.loc}"
                            locshopView.text = "Shop: ${detail.loc_shop}"
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
