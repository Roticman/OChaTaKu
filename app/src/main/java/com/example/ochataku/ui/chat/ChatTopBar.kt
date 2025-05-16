package com.example.ochataku.ui.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

// ---- Top Bar ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    peerId: Long,
    peerName: String,
    isGroup: Boolean,
    convId: Long,
    navController: NavController
) {
    TopAppBar(
        title = { Text(peerName) },
        actions = {
            IconButton(onClick = {
                val route = if (isGroup) "groupDetail/$convId" else "contact_profile/$peerId"
                navController.navigate(route)
            }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        }
    )
}