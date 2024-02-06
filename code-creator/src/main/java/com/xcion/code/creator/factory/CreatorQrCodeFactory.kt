package com.xcion.code.creator.factory

import android.accounts.AccountsException
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import com.xcion.code.creator.enums.QRCodeParticle
import com.xcion.code.creator.utils.DotQRCodeUtil
import com.xcion.code.creator.utils.PixelQRCodeUtil


/**
 * @author: KernHu
 * @date: 2024/2/1
 * @Description: 创建二维码的工厂类
 */
class CreatorQrCodeFactory : AbstractQrCodeFactory() {

    @Throws(Exception::class)
    override fun generateBitmap(content: String): Bitmap? {
        return generateBitmap(content, DEFAULT_QRCODE_WIDTH, DEFAULT_QRCODE_HEIGHT)
    }

    @Throws(Exception::class)
    override fun generateBitmap(content: String, width: Int, height: Int): Bitmap? {
        return generateBitmap(content, width, height, BLACK.toInt())
    }

    override fun generateBitmap(content: String, width: Int, height: Int, color: Int): Bitmap? {
        return generateBitmap(content, width, height, color, 0, null)
    }

    override fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        qrcodeParticle: QRCodeParticle?
    ): Bitmap? {
        return generateBitmap(content, width, height, color, 0, null, qrcodeParticle)
    }

    @Throws(Exception::class)
    override fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        logoSize: Int,
        logo: Bitmap?
    ): Bitmap? {
        return generateBitmap(content, width, height, color, logoSize, logo, QRCodeParticle.PIXEL)
    }

    @Throws(Exception::class)
    override fun generateBitmap(
        content: String,
        width: Int,
        height: Int,
        color: Int,
        logoSize: Int,
        logo: Bitmap?,
        qrcodeParticle: QRCodeParticle?
    ): Bitmap? {
        if (TextUtils.isEmpty(content)) {
            throw AccountsException("the content can't be null")
        }
        if (width == null || width == 0) {
            throw AccountsException("the width can't be null")
        }
        if (height == null || height == 0) {
            throw AccountsException("the height can't be null")
        }
        if (logo != null && (logoSize == null || logoSize == 0)) {
            throw AccountsException("the logoSize can't be null")
        }
        return if (qrcodeParticle == QRCodeParticle.DOT)
            DotQRCodeUtil.generateQRCodeImage(
                content,
                width,
                height,
                color,
                logoSize,
                logo,
                qrcodeFormat
            ) else
            PixelQRCodeUtil.generateBitmap(
                content,
                width,
                height,
                color,
                logoSize,
                logo,
                qrcodeFormat
            )
    }
}