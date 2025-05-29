package com.example.ochataku.ui.contact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ochataku.R
import com.example.ochataku.data.local.contact.ContactEntity
import com.example.ochataku.service.ApiClient.BASE_URL
import com.example.ochataku.model.ContactSimple
import com.example.ochataku.utils.getFirstLetter
import com.example.ochataku.viewmodel.ContactViewModel

@Composable
fun ContactList(
    navController: NavController,
    viewModel: ContactViewModel,
    contacts: List<ContactEntity>,
    userMap: Map<Long, ContactSimple>,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val processedContacts = contacts.map { contact ->
        val user = userMap.values.find { user -> user.user_id == contact.peerId }
        val displayName =
            contact.remarkName?.takeIf { it.isNotBlank() } ?: user?.username ?: "未知用户"
        displayName to contact
    }

    val grouped = processedContacts.groupBy { (displayName, _) ->
        getFirstLetter(displayName)
    }.toSortedMap()

    var itemIndex = 0
    val letterIndexMap = remember { mutableMapOf<String, Int>() }

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            ContactStaticItem(
                title = stringResource(R.string.new_friend),
                onClick = { navController.navigate("friend_request") })
            itemIndex++
        }
        item {
            ContactStaticItem(
                title = stringResource(R.string.group_chat),
                onClick = { navController.navigate("group_list") })
            itemIndex++
        }

        grouped.forEach { (letter, contactsInGroup) ->
            letterIndexMap[letter] = itemIndex

            item {
                GroupTitle(letter)
                itemIndex++
            }

            items(contactsInGroup) { (displayName, contact) ->
                val user = userMap.values.find { user -> user.user_id == contact.peerId }
                ContactItem(
                    navController = navController,
                    contact = contact,
                    username = user?.username,
                    avatarUrl = user?.avatar
                )
                itemIndex++
            }
        }
    }

    // 最后把字母映射保存进ViewModel
    LaunchedEffect(letterIndexMap) {
        viewModel.updateLetterIndexMap(letterIndexMap)
    }
}

@Composable
fun ContactStaticItem(title: String, onClick: (() -> Unit)? = null) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(16.dp),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun GroupTitle(letter: String) {
    Text(
        text = letter,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ContactItem(
    navController: NavController,
    contact: ContactEntity,
    username: String?,
    avatarUrl: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                contact.peerId.let {
                    navController.navigate("contact_profile/${it}")
                }
            }
            .padding(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${BASE_URL}$avatarUrl")
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            placeholder = painterResource(R.drawable.default_avatar),
            error = painterResource(R.drawable.ic_avatar_error),
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = contact.remarkName.takeIf { !it.isNullOrEmpty() } ?: (username ?: stringResource(
                R.string.unknown_user
            )),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
}
