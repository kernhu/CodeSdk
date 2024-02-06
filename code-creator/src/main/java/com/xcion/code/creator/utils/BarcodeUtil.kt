package com.xcion.code.creator.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.xcion.code.creator.factory.AbstractFactory

/**
 * @author: huming
 * @date: 2024/2/2
 * @Description: java类作用描述
 */
class BarcodeUtil {

    companion object {

        fun generateBitmap(
            content: String,
            width: Int,
            height: Int,
            color: Int,
            barcodeFormat: BarcodeFormat
        ): Bitmap? {

            var writer = MultiFormatWriter()
            var matrix: BitMatrix? = null
            try {
                matrix = writer.encode(content, barcodeFormat, width, height, null)
            } catch (e: WriterException) {
                e.printStackTrace()
                throw e
            }
            var pixelWidth = matrix?.width!!
            var pixelHeight = matrix?.height!!
            var pixels = IntArray(pixelWidth * pixelHeight)
            for (x in 0 until pixelWidth) {
                for (y in 0 until pixelHeight) {
                    pixels[x + (y * pixelWidth)] = (if (matrix.get(
                            x,
                            y
                        )
                    ) color else AbstractFactory.WHITE).toInt()
                }
            }
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            return bitmap
        }
    }
}