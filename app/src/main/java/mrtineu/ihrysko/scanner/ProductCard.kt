package mrtineu.ihrysko.scanner // Ensure this package matches your project structure

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // Import this
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // For placeholder, replace with Coil/Glide
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// If mrtineu.ihrysko.scanner.ui.theme.MyApplicationTheme is your theme, import it for previews
import mrtineu.ihrysko.scanner.ui.theme.MyApplicationTheme // Assuming this is your theme

// --- Data Class Definition ---
data class Product(
    val item_id: String,
    val name: String, // Combined name, edition is removed
    val price: String,
    val stock: String,
    val image: String, // HTTP resource URL
    val loc: String,
    val loc_shop: String,
    val ean: String
)

// --- Composable Implementation ---

@Composable
fun ProductCard(
    product: Product,
    initialExpanded: Boolean = false,
    modifier: Modifier = Modifier // Add modifier parameter
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var isLiked by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() } // Declare interactionSource here

    Card(
        modifier = modifier // Apply the passed modifier here
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = interactionSource, // Pass the interaction source
                indication = null, // Pass null to disable the ripple
                onClick = { isExpanded = !isExpanded }
            )
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (!isExpanded) {
                // --- Collapsed View ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Replace with Coil/Glide
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = product.stock,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.price,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // --- Expanded View ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Enlarged Image
                    Image(
                        painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Replace with Coil/Glide
                        contentDescription = "${product.name} enlarged",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit // Or ContentScale.Crop depending on desired look
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall, // More prominent name
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.price,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Code: ${product.loc}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Item ID: ${product.item_id}", style = MaterialTheme.typography.bodyMedium)
                    Text("Shop Location: ${product.loc_shop}", style = MaterialTheme.typography.bodyMedium)
                    Text("EAN: ${product.ean}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isLiked = !isLiked
                            // TODO: Implement like action (e.g., call ViewModel)
                        }) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preview ---
@Preview(name = "Product Card Collapsed", showBackground = true)
@Composable
fun DefaultProductCardPreview() {
    val sampleProduct = Product(
        item_id = "123",
        name = "Osadníci z Katanu", // Edition info removed/merged
        loc = "H46/RR87",
        price = "19.99$",
        stock = "0/1",
        image = "http://example.com/catan.png",
        loc_shop = "Main Warehouse",
        ean = "1234567890123"
    )
    MyApplicationTheme { // Use your app's theme
        ProductCard(
            product = sampleProduct,
            modifier = Modifier.padding(16.dp) // Example usage in preview
        )
    }
}

@Preview(name = "Product Card Expanded", showBackground = true)
@Composable
fun ProductCardExpandedPreview() {
    val sampleProduct = Product(
        item_id = "123",
        name = "Osadníci z Katanu", // Edition info removed/merged
        loc = "H46/RR87",
        price = "19.99$",
        stock = "0/1",
        image = "http://example.com/catan.png",
        loc_shop = "Main Warehouse",
        ean = "1234567890123"
    )
    MyApplicationTheme { // Use your app's theme
        ProductCard(
            product = sampleProduct,
            initialExpanded = true,
            modifier = Modifier.padding(16.dp) // Example usage in preview
        )
    }
}