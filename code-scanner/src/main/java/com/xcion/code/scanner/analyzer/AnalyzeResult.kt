package com.xcion.code.scanner.analyzer

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.annotation.Nullable
import com.xcion.code.scanner.bean.FrameMetadata
import com.xcion.code.scanner.utils.BitmapUtils


/**
 * @date: 2024/1/22
 * @Description: 分析结果实体类
 */
class AnalyzeResult<T>(
    imageData: ByteArray,
    imageFormat: Int,
    frameMetadata: FrameMetadata,
    result: T
) {

    private val imageData: ByteArray

    private val imageFormat: Int

    private val frameMetadata: FrameMetadata

    private var bitmap: Bitmap? = null

    private val result: T

    fun getImageData(): ByteArray {
        return imageData
    }

    fun getImageFormat(): Int {
        return imageFormat
    }

    fun getFrameMetadata(): FrameMetadata {
        return frameMetadata
    }

    @Nullable
    fun getBitmap(): Bitmap? {
        require(imageFormat == ImageFormat.NV21) { "only support ImageFormat.NV21 for now." }
        if (bitmap == null) {
            bitmap = BitmapUtils.getBitmap(imageData, frameMetadata)
        }
        return bitmap
    }

    fun getBitmapWidth(): Int {
        return if (frameMetadata.getRotation() % 180 === 0) {
            frameMetadata.getWidth()
        } else frameMetadata.getHeight()
    }

    fun getBitmapHeight(): Int {
        return if (frameMetadata.getRotation() % 180 === 0) {
            frameMetadata.getHeight()
        } else frameMetadata.getWidth()
    }

    fun getResult(): T {
        return result
    }

    init {
        this.imageData = imageData
        this.imageFormat = imageFormat
        this.frameMetadata = frameMetadata
        this.result = result
    }
}
