package com.example.ochataku.model

import com.example.ochataku.model.PinyinUtil.getPinyinFirstLetter
import com.example.ochataku.model.PinyinUtil.isChinese

fun getFirstLetter(name: String): String {
    if (name.isEmpty()) return "#"

    val c = name[0]

    return when {
        c.isLetter() -> c.uppercaseChar().toString() // 英文直接拿
        c.isDigit() -> "#" // 数字归到"#"
        isChinese(c) -> getPinyinFirstLetter(c)
        else -> "#" // 其他符号归到"#"
    }
}
