package com.example.ochataku.ui.contact

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ochataku.R
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ContactRequest
import com.example.ochataku.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupListScreen(
    userId: Long,
    navController: NavController
) {
    val viewModel: GroupViewModel = hiltViewModel()
    val groupList by viewModel.groupList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchText = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadGroups(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ✅ 搜索框（预留）
        OutlinedTextField(
            value = searchText.value,
            onValueChange = { searchText.value = it },
            label = { Text("搜索群聊（尚未启用）") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Text("群聊列表", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Text("加载中...")
        } else if (groupList.isEmpty()) {
            Text("暂无群聊")
        } else {
            groupList.forEach { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch {
                                try {
                                    val request = ContactRequest(
                                        userId = userId,
                                        peerId = group.groupId,
                                        isGroup = true
                                    )
                                    val response = viewModel.getOrCreateConversation(request)
                                    val convId = response.convId
                                    val encodedName = Uri.encode(group.groupName)
                                    val encodedAvatar = Uri.encode(group.avatar ?: "")
                                    navController.navigate("chat/$convId/${group.groupId}/$encodedName/true/$encodedAvatar")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ApiClient.BASE_URL + (group.avatar ?: ""),
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.default_avatar),
                        error = painterResource(id = R.drawable.default_avatar),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
