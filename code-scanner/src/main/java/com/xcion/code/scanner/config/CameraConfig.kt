package com.xcion.code.scanner.config

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview


/**
 * 相机配置：主要用于提供相机预览时可自定义一些配置，便于拓展；
 * <p>
 * 库中内置实现{@link CameraConfig}的有{@link AdaptiveCameraConfig}、{@link AspectRatioCameraConfig}和{@link ResolutionCameraConfig}；
 * <p>
 * 这里简单说下各自的特点：
 * <p>
 * {@link CameraConfig} - CameraX默认的相机配置
 * <p>
 * {@link AdaptiveCameraConfig} - 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置
 * <p>
 * {@link AspectRatioCameraConfig} - 根据纵横比配置相机，使输出分析的图像尽可能的接近屏幕的比例
 * <p>
 * {@link ResolutionCameraConfig} - 根据尺寸配置相机的目标图像大小，使输出分析的图像的分辨率尽可能的接近屏幕尺寸
 * <p>
 * 当使用默认的 {@link CameraConfig}在某些机型上体验欠佳时，你可以尝试使用{@link AdaptiveCameraConfig}、{@link AspectRatioCameraConfig}或
 * {@link ResolutionCameraConfig}会有意想不到奇效。
 * <p>
 * 你也可以自定义或覆写 {@link CameraConfig} 中的 {@link #options} 方法，根据需要定制配置。
 *
 */
open class CameraConfig {

    fun CameraConfig() {}

    /**
     * 配置 [CameraSelector.Builder]；可参考：[AspectRatioCameraConfig] 或 [ResolutionCameraConfig]
     *
     *
     * 如配置前置摄像头：`builder.requireLensFacing(CameraSelector.LENS_FACING_FRONT)`
     *
     *
     * 切记，外部请勿直接调用 [.options]
     *
     * @param builder
     * @return
     */
    open fun options(builder: CameraSelector.Builder): CameraSelector {
        return builder.build()
    }

    /**
     * 配置 [Preview.Builder]；可参考：[AspectRatioCameraConfig] 或 [ResolutionCameraConfig]
     *
     *
     * 如配置目标旋转角度为90度：`builder.setTargetRotation(Surface.ROTATION_90)`
     *
     *
     * 切记，外部请勿直接调用 [.options]
     *
     * @param builder
     * @return
     */
    open fun options(builder: Preview.Builder): Preview {
        return builder.build()
    }

    /**
     * 配置 [ImageAnalysis.Builder]；可参考：[AspectRatioCameraConfig] 或 [ResolutionCameraConfig]
     *
     *
     * 如配置目标旋转角度为90度：`builder.setTargetRotation(Surface.ROTATION_90)`
     *
     *
     * 切记，外部请勿直接调用 [.options]
     *
     * @param builder
     * @return
     */
    open fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        return builder.build()
    }

}