package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ochataku.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContactsViewModel : ViewModel() {
    private val _contacts = MutableStateFlow<List<Contact>>(
        listOf(
            Contact(
                1,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                2,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(3, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                4,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                5,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(6, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                7,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                8,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(9, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                10,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                11,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(12, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                13,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                14,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(15, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                16,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                17,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(18, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
            Contact(
                19,
                "张三",
                "13333333333",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(
                20,
                "李四",
                "14444444444",
                "https://img.shetu66.com/2023/04/25/1682391069844152.png"
            ),
            Contact(21, "王二麻子", "12222222222", "https://www.keaitupian.cn/cjpic/frombd/1/253/2365900073/158321866.jpg"), // 没有头像
        )
    )

    val contacts: StateFlow<List<Contact>> = _contacts
}
