package com.xcion.code.scanner.analyzer

import android.graphics.Bitmap
import androidx.annotation.Nullable
import androidx.camera.core.ImageProxy


/**
 * @date: 2024/1/22
 * @Description:分析器：主要用于分析相机预览的帧数据
 */
open interface Analyzer<T> {
    /**
     * 分析图像并将分析的结果通过分析监听器返回
     *
     * @param imageProxy 需要分析的图像
     * @param listener   分析监听器，参见：[OnAnalyzeListener]
     */
    fun analyze(imageProxy: ImageProxy, listener: OnAnalyzeListener<T>)

    /**
     * 分析图像并将分析的结果通过分析监听器返回
     *
     * @param bitmap 需要分析的图像
     * @param listener   分析监听器，参见：[OnAnalyzeListener]
     */
    fun analyze(bitmap: Bitmap, listener: OnAnalyzeListener<T>)

    /**
     * Analyze listener
     *
     * @param <T> 泛型T为分析结果对应的对象
    </T> */
    interface OnAnalyzeListener<T> {
        /**
         * 成功
         *
         * @param result 分析结果
         */
        fun onSuccess(result: AnalyzeResult<T>)

        /**
         * 失败
         *
         * @param e 异常
         */
        fun onFailure(@Nullable e: Exception?)
    }
}