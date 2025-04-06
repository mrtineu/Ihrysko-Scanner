@file:OptIn(ExperimentalMaterial3Api::class)

package mrtineu.ihrysko.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults

@Composable
fun MyTopBar() {
    val (searchText, setSearchText) = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val (menuExpanded, setMenuExpanded) = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val (isFavorite, setIsFavorite) = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    androidx.compose.material3.TopAppBar(
        title = {
            androidx.compose.material3.OutlinedTextField(
                value = searchText,
                onValueChange = { setSearchText(it) },
                label = { androidx.compose.material3.Text("Search") },
                singleLine = true
            )
        },
        actions = {
            androidx.compose.material3.IconButton(onClick = { setIsFavorite(!isFavorite) }) {
                androidx.compose.material3.Icon(
                    imageVector = if (isFavorite) androidx.compose.material.icons.Icons.Filled.Favorite else androidx.compose.material.icons.Icons.Outlined.Favorite,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray
                )
            }
            androidx.compose.material3.IconButton(onClick = { setMenuExpanded(!menuExpanded) }) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }
            androidx.compose.material3.DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { setMenuExpanded(false) }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    text = { androidx.compose.material3.Text("Settings") },
                    onClick = { /* Not implemented */ }
                )
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.List,
                            contentDescription = "WishList"
                        )
                    },
                    text = { androidx.compose.material3.Text("WishList") },
                    onClick = { /* Not implemented */ }
                )
            }
        },
    )
}
