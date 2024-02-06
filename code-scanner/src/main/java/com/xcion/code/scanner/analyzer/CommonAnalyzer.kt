package com.xcion.code.scanner.analyzer

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.xcion.code.scanner.bean.FrameMetadata
import com.xcion.code.scanner.analyzer.Analyzer.OnAnalyzeListener
import com.xcion.code.scanner.utils.ImageUtils
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @date: 2024/1/23
 * @Description: java类作用描述
 */
abstract class CommonAnalyzer<T> : Analyzer<T> {


    private val queue: Queue<ByteArray> = ConcurrentLinkedQueue()

    private val joinQueue: AtomicBoolean = AtomicBoolean(false)

    /**
     * 检测图像
     *
     *
     * MLKit的各个子库只需实现此方法即可；通常为：`return detector.process(inputImage)`
     *
     * @param inputImage [InputImage]
     * @return [Task]
     */
    protected abstract fun detectInImage(inputImage: InputImage?): Task<List<Barcode?>?>

    /***
     * 从imageProxy中解析
     * */
    override fun analyze(imageProxy: ImageProxy, listener: OnAnalyzeListener<T>) {
        if (!joinQueue.get()) {
            val imageSize = imageProxy.width * imageProxy.height
            val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
            queue.add(bytes)
            joinQueue.set(true)
        }
        if (queue.isEmpty()) {
            return
        }
        val nv21Data: ByteArray = queue.poll()
        try {
            ImageUtils.yuv_420_888toNv21(imageProxy, nv21Data)
            val inputImage = InputImage.fromByteArray(
                nv21Data,
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees,
                InputImage.IMAGE_FORMAT_NV21
            )
            // 检测分析
            detectInImage(inputImage).addOnSuccessListener { result ->
                if (isNullOrEmpty(result)) {
                    queue.add(nv21Data)
                    listener.onFailure(null)
                } else {
                    val frameMetadata = FrameMetadata(
                        imageProxy.width,
                        imageProxy.height,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    joinQueue.set(false)
                    listener.onSuccess(
                        AnalyzeResult<T>(
                            nv21Data,
                            ImageFormat.NV21,
                            frameMetadata,
                            result as T
                        )
                    )
                }
            }.addOnFailureListener { e ->
                queue.add(nv21Data)
                listener.onFailure(e)
            }
        } catch (e: Exception) {
            queue.add(nv21Data)
            listener.onFailure(e)
        }
    }

    /***
     * 从bitmap中解析
     * */
    override fun analyze(bitmap: Bitmap, listener: OnAnalyzeListener<T>) {
        if (!joinQueue.get()) {
            val imageSize = bitmap.width * bitmap.height
            val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
            queue.add(bytes)
            joinQueue.set(true)
        }
        if (queue.isEmpty()) {
            return
        }
        val nv21Data: ByteArray = queue.poll()
        val frameMetadata = FrameMetadata(
            bitmap.width,
            bitmap.height,
            0
        )

        try {
            var inputImage = InputImage.fromBitmap(bitmap, 0)
            BarcodeScanning.getClient().process(inputImage).addOnSuccessListener {
                if (isNullOrEmpty(it)) {
                    queue.add(nv21Data)
                    listener.onFailure(null)
                } else {
                    joinQueue.set(false)
                    listener.onSuccess(
                        AnalyzeResult<T>(
                            nv21Data,
                            ImageFormat.NV21,
                            frameMetadata,
                            it as T
                        )
                    )
                }
            }.addOnFailureListener {
                queue.add(nv21Data)
                listener.onFailure(it)
            }
        } catch (e: Exception) {
            queue.add(nv21Data)
            listener.onFailure(e)
        }
    }

    /**
     * 是否为空
     *
     * @param obj
     * @return
     */
    public open fun isNullOrEmpty(obj: Any?): Boolean {
        if (obj == null) {
            return true
        }
        return if (obj is Collection<*>) {
            obj.isEmpty()
        } else false
    }
}