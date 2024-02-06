package com.xcion.code.creator.factory

import android.accounts.AccountsException
import android.graphics.Bitmap
import android.text.TextUtils
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.xcion.code.creator.utils.BarcodeUtil

/**
 * @author: KernHu
 * @date: 2024/2/1
 * @Description: 创建条形码的工厂类
 */
class CreatorBarcodeFactory : AbstractBarcodeFactory() {

    @Throws(Exception::class)
    override fun generateBitmap(content: String): Bitmap? {
        return generateBitmap(content, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT)
    }

    @Throws(Exception::class)
    override fun generateBitmap(content: String, width: Int, height: Int): Bitmap? {
        return generateBitmap(content, width, height, BLACK.toInt())
    }

    override fun generateBitmap(content: String, width: Int, height: Int, color: Int): Bitmap? {
        if (TextUtils.isEmpty(content)) {
            throw AccountsException("the content can't be null")
        }
        if (width == null || width == 0) {
            throw AccountsException("the width can't be null")
        }
        if (height == null || height == 0) {
            throw AccountsException("the height can't be null")
        }
        return BarcodeUtil.generateBitmap(content, width, height, color, barcodeFormat)
    }
}