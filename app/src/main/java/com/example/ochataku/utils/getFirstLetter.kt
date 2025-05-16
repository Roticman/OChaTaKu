package com.example.ochataku.utils

import com.example.ochataku.utils.PinyinUtil.getPinyinFirstLetter
import com.example.ochataku.utils.PinyinUtil.isChinese

fun getFirstLetter(name: String?): String {
    if (name.isNullOrBlank()) return "#"

    val c = name[0]

    return when {
        c.isLetter() -> c.uppercaseChar().toString() // 英文直接拿
        c.isDigit() -> "#" // 数字归到 "#"
        isChinese(c) -> getPinyinFirstLetter(c)
        else -> "#" // 其他符号归到 "#"
    }
}

