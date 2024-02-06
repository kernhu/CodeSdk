package com.xcion.code.creator.factory

import android.graphics.Bitmap

/**
 * @author: huming
 * @date: 2024/2/1
 * @Description: 接口
 */
interface IFactory {

    fun generateBitmap(content: String): Bitmap?

    fun generateBitmap(content: String, width: Int, height: Int): Bitmap?

    fun generateBitmap(content: String, width: Int, height: Int, color: Int): Bitmap?
}