package mrtineu.ihrysko.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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
                Scaffold(
                    topBar = { MyTopBar() },
                    floatingActionButton = {
                        LargeFloatingActionButton(
                            onClick = {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        print("Scanned barcode: ${barcode.rawValue}")
                                    }
                                    .addOnCanceledListener {
                                        // Task canceled
                                    }
                                    .addOnFailureListener { e ->
                                        // Task failed with an exception
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
                    ProductCard(
                        product = Product(
                            item_id = "1",
                            name = "Sample Product that is quite long and needs to be truncated",
                            price = "$19.99",
                            stock = "In Stock",
                            image = "https://example.com/image.jpg", // Placeholder URL
                            loc = "item123",
                            loc_shop = "Shop A",
                            ean = "1234567890123"
                        ),
                        initialExpanded = false,
                        modifier = Modifier.padding(innerPadding)
                    )
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
