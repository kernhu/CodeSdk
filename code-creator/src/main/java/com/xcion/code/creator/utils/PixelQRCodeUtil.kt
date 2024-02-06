package com.xcion.code.creator.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.xcion.code.creator.factory.AbstractFactory
import java.util.Hashtable

/**
 * @author: KernHu
 * @date: 2024/2/2
 * @Description: 生成正方形二维码+带LOGO二维码
 */
class PixelQRCodeUtil {

    companion object {
        fun generateBitmap(
            content: String,
            width: Int,
            height: Int,
            color: Int,
            logoSize: Int,
            logo: Bitmap?,
            qrcodeFormat: BarcodeFormat
        ): Bitmap? {

            var logoTemp: Bitmap? = null
            if (logo != null) {
                logoTemp = zoomBitmap(logo, logoSize)
            }

            var hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 1
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

            var writer = MultiFormatWriter()
            var matrix: BitMatrix? = null
            try {
                matrix = writer.encode(content, qrcodeFormat, width, height, hints)
            } catch (e: WriterException) {
                e.printStackTrace()
                throw e
            }

            var pixelWidth = matrix?.width!!
            var pixelHeight = matrix?.height!!
            var halfW = pixelWidth / 2
            var halfH = pixelHeight / 2
            var pixels = IntArray(pixelWidth * pixelHeight)
            for (y in 0 until pixelHeight) {
                for (x in 0 until pixelWidth) {
                    if (logoTemp == null) {
                        pixels[y * pixelWidth + x] = (if (matrix.get(
                                x,
                                y
                            )
                        ) color else AbstractFactory.WHITE).toInt()
                    } else {
                        if (x > halfW - logoSize && x < halfW + logoSize && y > halfH - logoSize && y < halfH + logoSize) {
                            pixels[y * pixelWidth + x] =
                                logoTemp?.getPixel(x - halfW + logoSize, y - halfH + logoSize)!!
                        } else {
                            pixels[y * pixelWidth + x] =
                                (if (matrix.get(
                                        x,
                                        y
                                    )
                                ) color else AbstractFactory.WHITE).toInt()
                        }
                    }
                }
            }
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, pixelWidth, 0, 0, pixelWidth, pixelHeight)
            return bitmap
        }

        @Throws(Exception::class)
        fun zoomBitmap(logo: Bitmap, size: Int): Bitmap? {
            var matrix = Matrix()
            var sizeX = 2 * size / logo.width.toFloat()
            var sizeY = 2 * size / logo.height.toFloat()
            matrix.setScale(sizeX, sizeY)
            return Bitmap.createBitmap(logo, 0, 0, logo.width, logo.height, matrix, false)
        }
    }
}