package com.example.ochataku.utils

import net.sourceforge.pinyin4j.PinyinHelper

object PinyinUtil {


    fun getPinyinFirstLetter(c: Char): String {
        val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c)
        return if (pinyinArray != null && pinyinArray.isNotEmpty()) {
            pinyinArray[0][0].uppercaseChar().toString() // 取拼音第一个字母
        } else {
            "#"
        }
    }

    fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
    }
}

