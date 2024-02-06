package com.xcion.code.creator

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.xcion.code.creator.enums.CodeFormat
import com.xcion.code.creator.enums.QRCodeParticle
import com.xcion.code.creator.factory.AbstractFactory
import com.xcion.code.creator.factory.CreatorBarcodeFactory
import com.xcion.code.creator.factory.CreatorQrCodeFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author: KernHu
 * @date: 2024/2/1
 * @Description: 条码/二维码生成器
 */
class CodeCreator {


    public interface Callback {

        fun onSuccess(bitmap: Bitmap?)

        fun onFailure(e: Exception?)

    }

    companion object {

        public fun with(): CodeCreator {
            return CodeCreator()
        }

    }

    private var codeFormat: CodeFormat? = CodeFormat.QR_CODE
    private var width = 0
    private var height = 0
    private var content: String = ""
    private var color: Int = AbstractFactory.BLACK.toInt()
    private var qrcodeParticle: QRCodeParticle? = QRCodeParticle.PIXEL

    private var logo: Bitmap? = null
    private var logoSize: Int = 0
    private var callback: Callback? = null
    private var factory: AbstractFactory? = null
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var handler: Handler? = Handler(Looper.getMainLooper())

    /**
     * the format of the code
     * */
    public fun setCodeFormat(codeFormat: CodeFormat): CodeCreator {
        this.codeFormat = codeFormat
        return this
    }

    /**
     * the content string of the code
     * */
    public fun setContent(content: String): CodeCreator {
        this.content = content
        return this
    }

    /**
     * the width of the code
     * */
    public fun setWidth(width: Int): CodeCreator {
        this.width = width
        return this
    }

    /**
     * the height of the code
     * */
    public fun setHeight(height: Int): CodeCreator {
        this.height = height
        return this
    }

    /**
     * the color of the code
     * */
    public fun setColor(color: Int): CodeCreator {
        this.color = color
        return this
    }

    /**
     * the particle of the qrcode,it just take effect for qrcode.
     * */
    public fun setQRCodeParticle(qrcodeParticle: QRCodeParticle): CodeCreator {
        this.qrcodeParticle = qrcodeParticle
        return this
    }

    /**
     * the logo of the code when you need add logo in the center of qrcode,it just take effect for qrcode.
     * */
    public fun setLogo(logo: Bitmap): CodeCreator {
        this.logo = logo
        return this
    }

    /**
     * the logo size for the logo
     * */
    public fun setLogoSize(logoSize: Int): CodeCreator {
        this.logoSize = logoSize
        return this
    }

    /**
     * the listener when generate the code will be called
     * */
    public fun setCallback(callback: Callback): CodeCreator {
        this.callback = callback
        return this
    }

    @Synchronized
    public fun generate() {
        handler?.removeCallbacks(recycleRunnable)
        executorService.execute {
            var bitmap: Bitmap? = null
            try {
                if (codeFormat == CodeFormat.QR_CODE) {
                    width = if (width != 0) width else AbstractFactory.DEFAULT_QRCODE_WIDTH
                    height = if (height != 0) height else AbstractFactory.DEFAULT_QRCODE_HEIGHT
                    factory = CreatorQrCodeFactory()

                    if (logo == null) {
                        bitmap =
                            (factory as CreatorQrCodeFactory).generateBitmap(
                                content,
                                width,
                                height,
                                color,
                                qrcodeParticle
                            )
                    } else {
                        bitmap = (factory as CreatorQrCodeFactory).generateBitmap(
                            content,
                            width,
                            height,
                            color,
                            logoSize,
                            logo,
                            qrcodeParticle
                        )
                    }
                } else {
                    width = if (width != 0) width else AbstractFactory.DEFAULT_BARCODE_WIDTH
                    height = if (height != 0) height else AbstractFactory.DEFAULT_BARCODE_HEIGHT
                    factory = CreatorBarcodeFactory()
                    bitmap =
                        (factory as CreatorBarcodeFactory).generateBitmap(
                            content,
                            width,
                            height,
                            color
                        )
                }
                successOnUiThread(bitmap!!)
            } catch (e: Exception) {
                failureOnUiThread(e)
            }
        }
    }

    private fun successOnUiThread(bitmap: Bitmap) {
        handler?.post {
            callback?.onSuccess(bitmap)
        }
        handler?.postDelayed(recycleRunnable, 1000)
    }

    private fun failureOnUiThread(exception: Exception) {
        handler?.post {
            callback?.onFailure(exception)
        }
        handler?.postDelayed(recycleRunnable, 1000)
    }

    private var recycleRunnable = Runnable {
        callback = null
        factory = null
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
}