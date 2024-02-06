package com.xcion.code.scanner

import androidx.annotation.Nullable
import androidx.camera.core.Camera

/**
 * @date: 2024/1/23
 * @Description: 相机相关接口
 */
interface ICamera {
    /**
     * 启动相机预览
     */
    fun startCamera()

    /**
     * 停止相机预览
     */
    fun stopCamera()

    /**
     * 获取 [Camera]
     *
     * @return [Camera]
     */
    @Nullable
    fun getCamera(): Camera?

    /**
     * 释放
     */
    fun release()


}