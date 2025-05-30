package mrtineu.ihrysko.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch
import mrtineu.ihrysko.scanner.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8)
            .build()
        val scanner = GmsBarcodeScanning.getClient(this, options)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var productsListState by remember { mutableStateOf<List<Product>>(emptyList()) }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                Scaffold(
                    topBar = { MyTopBar() },
                    floatingActionButton = {
                        LargeFloatingActionButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                productsListState = emptyList() // Clear previous results
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        val ean = barcode.rawValue
                                        if (ean != null) {
                                            lifecycleScope.launch {
                                                try {
                                                    // getByEAN now returns List<Product>
                                                    val fetchedProducts: List<Product> = getByEAN(this@MainActivity, ean)
                                                    if (fetchedProducts.isNotEmpty()) {
                                                        productsListState = fetchedProducts
                                                    } else {
                                                        // getByEAN returned an empty list
                                                        productsListState = emptyList()
                                                        errorMessage = "No products found for EAN: $ean."
                                                    }
                                                } catch (e: Exception) {
                                                    productsListState = emptyList()
                                                    errorMessage = "Error fetching products: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        } else {
                                            errorMessage = "Scanned barcode has no value."
                                            isLoading = false
                                            productsListState = emptyList()
                                        }
                                    }
                                    .addOnCanceledListener {
                                        errorMessage = "Scan canceled."
                                        isLoading = false
                                        productsListState = emptyList()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Scan failed: ${e.message}"
                                        isLoading = false
                                        productsListState = emptyList()
                                    }
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else if (productsListState.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(productsListState) { product -> // Use productsListState directly
                                    ProductCard(
                                        product = product,
                                        initialExpanded = productsListState.size == 1,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Scan a barcode to search for products.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
