package com.xcion.code.scanner

import androidx.annotation.FloatRange




/**
 * @date: 2024/1/23
 * @Description: 相机控制
 */
interface ICameraControl {

    /**
     * 放大
     */
    fun zoomIn()

    /**
     * 缩小
     */
    fun zoomOut()

    /**
     * 缩放到指定比例
     *
     * @param ratio 缩放比例
     */
    fun zoomTo(ratio: Float)

    /**
     * 线性放大
     */
    fun lineZoomIn()

    /**
     * 线性缩小
     */
    fun lineZoomOut()

    /**
     * 线性缩放到指定比例
     *
     * @param linearZoom 线性缩放比例；范围在：0.0 ~ 1.0之间
     */
    fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float)

    /**
     * 设置闪光灯（手电筒）是否开启
     *
     * @param torch 是否开启闪光灯（手电筒）
     */
    fun enableTorch(torch: Boolean)

    /**
     * 闪光灯（手电筒）是否开启
     *
     * @return 闪光灯（手电筒）是否开启
     */
    fun isTorchEnabled(): Boolean

    /**
     * 是否支持闪光灯
     *
     * @return 是否支持闪光灯
     */
    fun hasFlashUnit(): Boolean
}