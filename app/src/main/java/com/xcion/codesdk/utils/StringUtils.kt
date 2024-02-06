package com.xcion.codesdk.utils

/**
 * @author: huming
 * @date: 2024/2/1
 * @Description: java类作用描述
 */
class StringUtils {

    companion object {
        private val pattern = Regex("[\\u4e00-\\u9fa5]")
        fun contains(content: String): Boolean {
            return pattern.containsMatchIn(content)
        }
    }
}