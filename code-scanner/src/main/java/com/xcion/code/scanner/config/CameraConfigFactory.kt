package com.xcion.code.scanner.config

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LensFacing


/**
 * @date: 2024/1/23
 * @Description: java类作用描述
 */
class CameraConfigFactory {


    private fun CameraConfigFactory() {
        throw AssertionError()
    }

    companion object {
        /**
         * 根据设备配置创建一个相匹配的CameraConfig；
         *
         *
         * 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置；
         * 在适配、性能与体验之间找到平衡点，最终创建一个比较适合当前设备的 CameraConfig。
         *
         * @param context    [Context]
         * @param lensFacing [CameraSelector.LENS_FACING_BACK] or [CameraSelector.LENS_FACING_FRONT]
         * @return 返回一个比较适合当前设备的 [CameraConfig]
         */

        @JvmStatic
        fun createDefaultCameraConfig(
            context: Context?,
            @LensFacing lensFacing: Int
        ): CameraConfig? {
            return object : AdaptiveCameraConfig(context) {
                override fun options(builder: CameraSelector.Builder): CameraSelector {
                    if (lensFacing != CameraSelector.LENS_FACING_UNKNOWN) {
                        builder.requireLensFacing(lensFacing)
                    }
                    return super.options(builder)
                }
            }
        }
    }
}