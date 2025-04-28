package com.example.ochataku.ui.contact

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ochataku.viewmodel.FriendRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen() {
    val context = LocalContext.current
    val viewModel: FriendRequestViewModel = hiltViewModel()
    val friendRequests by viewModel.friendRequests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFriendRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友请求") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(friendRequests) { request ->
                if (request.status == 0) { // 只展示待处理的
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "来自用户：${request.fromUserId}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = {
                                    viewModel.handleFriendRequest(request.id, "accept") {
                                        Toast.makeText(context, "已接受", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("同意")
                                }
                                Button(onClick = {
                                    viewModel.handleFriendRequest(request.id, "reject") {
                                        Toast.makeText(context, "已拒绝", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("拒绝")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
