package com.example.ochataku.ui.contact

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavController,) {
    val context = LocalContext.current
    val viewModel: ContactViewModel = hiltViewModel()
    val contacts by viewModel.contacts.collectAsState()
    val userMap by viewModel.userMap.collectAsState()
    val authManager = AuthManager(context)
    val listState = rememberLazyListState() // ⭐新增LazyListState
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadContacts(userId = authManager.getUserId())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("联系人") },
                actions = {
                    IconButton(onClick = { onAddContact(navController) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "添加联系人")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ContactList(
                navController = navController,
                viewModel = viewModel,
                contacts = contacts,
                userMap = userMap,
                state = listState
            )

            // 右边字母索引栏
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                val letters = ('A'..'Z').map { it.toString() }
                letters.forEach { letter ->
                    Text(
                        text = letter,
                        modifier = Modifier
                            .padding(2.dp)
                            .clickable {
                                coroutineScope.launch {
                                    viewModel.scrollToLetter(letter, listState)
                                }
                            },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun onAddContact(navController: NavController) {
    navController.navigate("friend_request")
}


