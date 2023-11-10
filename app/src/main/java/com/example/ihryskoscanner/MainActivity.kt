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
import android.graphics.BitmapFactory
import android.view.View
import android.util.Log
import android.widget.ImageButton
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
class MainActivity : Activity(), CoroutineScope {

    // Create a Job to manage all coroutines started in this Activity
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
        .build()
    private lateinit var job: Job

    // Override the CoroutineContext to include the job and the main dispatcher
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    private lateinit var changeTextButton: ImageButton
    private lateinit var itemContainer: LinearLayout
    private lateinit var imageView: ImageView
    private val search = Search()

    // Rest of your code...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the job
        job = Job()

        // Initialize views
        changeTextButton = findViewById(R.id.imageButton)
        itemContainer = findViewById(R.id.itemContainer)

        Log.d("INFO", "Initialized views")

        // Set an OnClickListener for the Button
        changeTextButton.setOnClickListener {
            // Call the changeText function as a coroutine
            GlobalScope.launch(Dispatchers.Main) {
                changeText()
            }
        }
        changeTextButton.visibility = View.VISIBLE
    }

    private fun changeText() {
        val scanner = GmsBarcodeScanning.getClient(this, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // Fetch XML content in the background
                launch { // Use launch to start a coroutine
                    val barcodev = barcode.rawValue.toString()
                    Log.d("INFO", "Barcode scanned $barcodev")
                    val xmlContent = search.fetchXMLContent()

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        processXMLContent(xmlContent, barcode)
                    }
                }
            }
            .addOnFailureListener { e ->
                itemContainer.removeAllViews()
                Log.d("ERROR", e.stackTraceToString())
            }
    }

    private suspend fun loadImageFromNetwork(imageUrl: String): Bitmap = withContext(Dispatchers.IO) {
        try {
            val inputStream = java.net.URL(imageUrl).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            // Handle any errors here, or simply rethrow the exception
            throw e
        }
    }

    private fun processXMLContent(xmlContent: String, barcode: Barcode) {
        val start = System.currentTimeMillis()
        Log.d("INFO", "XML search started ")
        val syncview = findViewById<TextView>(R.id.syncview)
        syncview.text = "Started search"
        val details = search.searchAndRetrieveDetails(barcode.rawValue.toString(), xmlContent)

        if (details != null) {
            // Clear previous items
            val nameTextView = findViewById<TextView>(R.id.nameTextView)
            val priceTextView = findViewById<TextView>(R.id.priceTextView)
            val idTextView = findViewById<TextView>(R.id.idTextView)
            val itemImageView = findViewById<ImageView>(R.id.itemImageView)

            nameTextView.text = "Name: ${details.name}"
            priceTextView.text = "Price: ${details.price}"
            idTextView.text = "ID: ${details.id}"

            Log.d("INFO", "Name: ${details.name}")

            // Load and display the image in the ImageView using a coroutine
            launch {
                try {
                    val imageUrl = details.image
                    val bitmap = loadImageFromNetwork(imageUrl)
                    itemImageView.setImageBitmap(bitmap)
                    Log.d("INFO", "Image rendered")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            changeTextButton.visibility = View.VISIBLE
        } else {
            // Handle barcode not found
            val nameTextView = findViewById<TextView>(R.id.nameTextView)

            nameTextView.text = "Name: Barcode not found"
            Log.d("WARNING", "Barcode not found")
            changeTextButton.visibility = View.VISIBLE
        }
        val time = System.currentTimeMillis() - start
        syncview.text = "Finished in $time ms"
        Log.d("INFO", "Rendering finished in $time")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel the job when the activity is destroyed
    }
}