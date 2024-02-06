package com.xcion.code.creator.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode


/**
 * @author: huming
 * @date: 2024/2/2
 * @Description: generate a Dot QRCode with logo
 */
class DotQRCodeUtil {


    companion object {

        @Throws(WriterException::class)
        fun generateQRCodeImage(
            content: String?,
            width: Int,
            height: Int,
            color: Int,
            logoSize: Int,
            logo: Bitmap?,
            qrcodeFormat: BarcodeFormat
        ): Bitmap? {
            val encodingHints: MutableMap<EncodeHintType, Any> = HashMap()
            encodingHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            val code: QRCode =
                Encoder.encode(content, ErrorCorrectionLevel.M, encodingHints)
            return renderQRImage(code, width, height, color, 1, logoSize, logo, qrcodeFormat)
        }

        private fun renderQRImage(
            code: QRCode,
            width: Int,
            height: Int,
            color: Int,
            quietZone: Int,
            logoSize: Int,
            logo: Bitmap?,
            qrcodeFormat: BarcodeFormat
        ): Bitmap? {

            var logoTemp: Bitmap? = null
            if (logo != null) {
                logoTemp = zoomBitmap(logo, logoSize)
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = color
            canvas.drawColor(Color.WHITE)
            val input = code.matrix ?: throw IllegalStateException()
            val inputWidth = input.width
            val inputHeight = input.height
            val qrWidth = inputWidth + quietZone * 2
            val qrHeight = inputHeight + quietZone * 2
            val outputWidth = Math.max(width, qrWidth)
            val outputHeight = Math.max(height, qrHeight)
            val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
            val leftPadding = (outputWidth - inputWidth * multiple) / 2
            val topPadding = (outputHeight - inputHeight * multiple) / 2
            val FINDER_PATTERN_SIZE = 7
            val CIRCLE_SCALE_DOWN_FACTOR = 30f / 30f
            val circleSize = (multiple * CIRCLE_SCALE_DOWN_FACTOR).toInt()
            val radius = circleSize * 1.0f / 2
            val cxStep = multiple / 2
            var inputY = 0
            var outputY = topPadding
            while (inputY < inputHeight) {
                var inputX = 0
                var outputX = leftPadding
                while (inputX < inputWidth) {
                    if (input[inputX, inputY].toInt() == 1) {
                        if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE || inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE || inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)) {
                            val cx = outputX + cxStep
                            val cy = outputY + cxStep
                            canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, paint)
                        }
                    }
                    inputX++
                    outputX += multiple
                }
                inputY++
                outputY += multiple
            }
            var circleDiameter = multiple * FINDER_PATTERN_SIZE
            circleDiameter /= 2
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                color,
                leftPadding,
                topPadding,
                circleDiameter
            )
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                color,
                leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple,
                topPadding,
                circleDiameter
            )
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                color,
                leftPadding,
                topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
                circleDiameter
            )

            return if (logoTemp == null) bitmap else addLogo(bitmap, logoTemp)
        }

        private fun drawFinderPatternCircleStyle(
            canvas: Canvas,
            paint: Paint,
            color: Int,
            x: Int,
            y: Int,
            circleDiameter: Int
        ) {
            var x = x
            var y = y
            val WHITE_CIRCLE_DIAMETER = circleDiameter * 5 / 7
            val MIDDLE_DOT_DIAMETER = circleDiameter * 3 / 7
            x += circleDiameter
            y += circleDiameter
            paint.color = color
            canvas.drawCircle(x.toFloat(), y.toFloat(), circleDiameter.toFloat(), paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x.toFloat(), y.toFloat(), WHITE_CIRCLE_DIAMETER.toFloat(), paint)
            paint.color = color
            canvas.drawCircle(x.toFloat(), y.toFloat(), MIDDLE_DOT_DIAMETER.toFloat(), paint)
        }

        @Throws(Exception::class)
        private fun zoomBitmap(logo: Bitmap, size: Int): Bitmap? {
            var matrix = Matrix()
            var sizeX = 2 * size / logo.width.toFloat()
            var sizeY = 2 * size / logo.height.toFloat()
            matrix.setScale(sizeX, sizeY)
            return Bitmap.createBitmap(logo, 0, 0, logo.width, logo.height, matrix, true)
        }

        private fun addLogo(originalBitmap: Bitmap?, logo: Bitmap?): Bitmap? {
            val bitmapWidth = originalBitmap?.width
            val bitmapHeight = originalBitmap?.height
            val logoWidth = logo?.width
            val logoHeight = logo?.height

            var bitmap: Bitmap? =
                Bitmap.createBitmap(bitmapWidth!!, bitmapHeight!!, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap!!)
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)
            canvas.scale(
                0.8F,
                0.8F,
                (bitmapWidth / 2).toFloat(),
                (bitmapHeight / 2).toFloat()
            )
            canvas.drawBitmap(
                logo!!,
                ((bitmapWidth - logoWidth!!) / 2).toFloat(),
                ((bitmapHeight - logoHeight!!) / 2).toFloat(),
                null
            )
            //Canvas.ALL_SAVE_FLAG
            canvas.save()
            canvas.restore()

            return bitmap
        }

    }


}