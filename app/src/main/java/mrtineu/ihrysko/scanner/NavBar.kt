@file:OptIn(ExperimentalMaterial3Api::class)

package mrtineu.ihrysko.scanner

import android.content.Context
import mrtineu.ihrysko.scanner.ui.theme.IhryskoOrange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.datatransport.BuildConfig
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Preference key for the API key
private val API_KEY_PREFERENCE = stringPreferencesKey("api_key")

@Composable
fun SettingsDialog(
    currentApiKey: String,
    onDismissRequest: () -> Unit,
    onApiKeySave: (String) -> Unit
) {
    var apiKeyText by remember(currentApiKey) { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Settings") },
        text = {
            Column {
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("App Version: ${AppConfig.VERSION}")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApiKeySave(apiKeyText)
                    onDismissRequest() // Close dialog after save
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MyTopBar() {
    val (searchText, setSearchText) = remember { mutableStateOf("") }
    val (menuExpanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val (isFavorite, setIsFavorite) = remember { mutableStateOf(false) } // Retained from your existing code

    var showSettingsDialog by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load API key from DataStore
    LaunchedEffect(key1 = context.dataStore) {
        context.dataStore.data
            .map { preferences ->
                preferences[API_KEY_PREFERENCE] ?: ""
            }
            .collect { loadedApiKey ->
                apiKey = loadedApiKey
            }
    }

    fun updateApiKeyInDataStore(newApiKey: String) {
        coroutineScope.launch {
            context.dataStore.edit { settings ->
                settings[API_KEY_PREFERENCE] = newApiKey
            }
            apiKey = newApiKey // Update local state
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = apiKey,
            onDismissRequest = { showSettingsDialog = false },
            onApiKeySave = { newKey ->
                updateApiKeyInDataStore(newKey)
            }
        )
    }

    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchText,
                onValueChange = { setSearchText(it) },
                label = { Text("Search") },
                singleLine = true
            )
        },
        actions = {
            IconButton(onClick = { setIsFavorite(!isFavorite) }) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "WishList",
                    tint = IhryskoOrange
                )
            }
            IconButton(onClick = { setMenuExpanded(!menuExpanded) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { setMenuExpanded(false) }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    text = { Text("Settings") },
                    onClick = {
                        setMenuExpanded(false) // Close the menu
                        showSettingsDialog = true // Open the dialog
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.List, // Assuming this is for WishList as per original
                            contentDescription = "WishList"
                        )
                    },
                    text = { Text("WishList") },
                    onClick = {
                        /* Not implemented */
                        setMenuExpanded(false) // Close the menu
                    }
                )
            }
        },
    )
}