package com.xcion.code.creator.factory

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.xcion.code.creator.enums.QRCodeParticle

/**
 * @author: huming
 * @date: 2024/2/1
 * @Description: 二维码工厂类
 */
abstract class AbstractQrCodeFactory : AbstractFactory() {

    protected val qrcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE

    abstract fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        qrcodeParticle: QRCodeParticle?
    ): Bitmap?


    abstract fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        logoSize: Int,
        logo: Bitmap?
    ): Bitmap?

    abstract fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        logoSize: Int,
        logo: Bitmap?,
        qrcodeParticle: QRCodeParticle?
    ): Bitmap?

}