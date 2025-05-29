package com.example.ochataku.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ochataku.R
import com.example.ochataku.model.ChatAgentState

// ---- Top Bar ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    currentUserId: Long,
    peerId: Long,
    peerName: String,
    isGroup: Boolean,
    convId: Long,
    navController: NavController
) {
    var showAgentDialog by remember { mutableStateOf(false) }
    val aiEnabled = ChatAgentState.isEnabled(currentUserId, convId)

    TopAppBar(
        title = { Text(peerName) },
        actions = {
            // 🔹 AI Icon: 切换亮暗资源
            IconButton(onClick = { showAgentDialog = true }) {
                Icon(
                    painterResource(id = if (aiEnabled) R.drawable.ic_robot_on else R.drawable.ic_robot),
                    contentDescription = "AI Agent",
                    tint = Color.Unspecified
                )
            }

            IconButton(onClick = {
                val route = if (isGroup) "groupDetail/$convId" else "contact_profile/$peerId"
                navController.navigate(route)
            }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        }
    )


    if (showAgentDialog) {
        AIAgentDialog(
            userId = currentUserId,
            convId = convId,
            onDismiss = { showAgentDialog = false }
        )

    }
}

@Composable
fun AIAgentDialog(userId: Long, convId: Long, onDismiss: () -> Unit) {
    var aiEnabled by remember { mutableStateOf(ChatAgentState.isEnabled(userId, convId)) }
    var userPrompt by remember { mutableStateOf(ChatAgentState.getPrompt(userId, convId)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.AI_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                    Text("${stringResource(R.string.open)}${stringResource(R.string.AI_title)}")
                }

                if (aiEnabled) {
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        label = { Text(stringResource(R.string.AI)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                ChatAgentState.setAgent(userId, convId, aiEnabled, userPrompt)
                onDismiss()
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

