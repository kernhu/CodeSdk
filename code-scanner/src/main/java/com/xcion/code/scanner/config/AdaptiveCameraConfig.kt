package com.xcion.code.scanner.config

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import com.xcion.code.scanner.CameraScan
import com.xcion.code.scanner.utils.LogUtils
import java.lang.String
import java.util.*
import kotlin.Int
import kotlin.collections.ArrayList


/**
 * @date: 2024/1/23
 * @Description: java类作用描述
 */
open class AdaptiveCameraConfig(context: Context?) : CameraConfig() {
    /**
     * 1080P
     */
    private val IMAGE_QUALITY_1080P = 1080

    /**
     * 720P
     */
    private val IMAGE_QUALITY_720P = 720

    private var mAspectRatioStrategy: AspectRatioStrategy? = null

    private var mPreviewQuality = 0
    private var mAnalysisQuality = 0
    private var mPreviewTargetSize: Size? = null
    private var mAnalysisTargetSize: Size? = null


    init {
        context?.let { initAdaptiveCameraConfig(it) }
    }
    /**
     * 初始化配置；根据 [DisplayMetrics] 获取屏幕尺寸来动态计算，从而找到合适的预览尺寸和分析尺寸
     *
     * @param context 上下文
     */
    private fun initAdaptiveCameraConfig(context: Context) {
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogUtils.d(String.format(Locale.getDefault(), "displayMetrics: %dx%d", width, height))
        if (width < height) {
            val ratio = height / width.toFloat()
            mPreviewQuality = Math.min(width, IMAGE_QUALITY_1080P)
            mAspectRatioStrategy =
                if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                } else {
                    AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
                }
            mPreviewTargetSize = Size(mPreviewQuality, Math.round(mPreviewQuality * ratio))
            mAnalysisQuality = if (width > IMAGE_QUALITY_1080P) {
                IMAGE_QUALITY_1080P
            } else {
                Math.min(width, IMAGE_QUALITY_720P)
            }
            mAnalysisTargetSize = Size(mAnalysisQuality, Math.round(mAnalysisQuality * ratio))
        } else {
            mPreviewQuality = Math.min(height, IMAGE_QUALITY_1080P)
            val ratio = width / height.toFloat()
            mAspectRatioStrategy =
                if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                } else {
                    AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
                }
            mPreviewTargetSize = Size(Math.round(mPreviewQuality * ratio), mPreviewQuality)
            mAnalysisQuality = if (height > IMAGE_QUALITY_1080P) {
                IMAGE_QUALITY_1080P
            } else {
                Math.min(height, IMAGE_QUALITY_720P)
            }
            mAnalysisTargetSize = Size(Math.round(mAnalysisQuality * ratio), mAnalysisQuality)
        }
    }

    override fun options(builder: CameraSelector.Builder): CameraSelector {
        return super.options(builder)
    }

    override fun options(builder: Preview.Builder): Preview {
        builder.setResolutionSelector(createPreviewResolutionSelector()!!)
        return super.options(builder)
    }

    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setResolutionSelector(createAnalysisResolutionSelector()!!)
        return super.options(builder)
    }

    /**
     * 创建预览 分辨率选择器；根据自适应策略，创建一个合适的 [ResolutionSelector]
     *
     * @return [ResolutionSelector]
     */
    private fun createPreviewResolutionSelector(): ResolutionSelector? {
        return ResolutionSelector.Builder()
            .setAspectRatioStrategy(mAspectRatioStrategy!!)
            .setResolutionStrategy(
                ResolutionStrategy(
                    mPreviewTargetSize!!,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .setResolutionFilter { supportedSizes: List<Size>, rotationDegrees: Int ->
                LogUtils.d("Preview supportedSizes: $supportedSizes")
                val list: MutableList<Size> = ArrayList()
                for (supportedSize in supportedSizes) {
                    val size = Math.min(supportedSize.width, supportedSize.height)
                    if (size <= mPreviewQuality) {
                        list.add(supportedSize)
                    }
                }
                list
            }
            .build()
    }

    /**
     * 创建分析 分辨率选择器；根据自适应策略，创建一个合适的 [ResolutionSelector]
     *
     * @return [ResolutionSelector]
     */
    private fun createAnalysisResolutionSelector(): ResolutionSelector? {
        return ResolutionSelector.Builder()
            .setAspectRatioStrategy(mAspectRatioStrategy!!)
            .setResolutionStrategy(
                ResolutionStrategy(
                    mAnalysisTargetSize!!,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .setResolutionFilter { supportedSizes: List<Size>, rotationDegrees: Int ->
                LogUtils.d("ImageAnalysis supportedSizes: $supportedSizes")
                val list: MutableList<Size> = ArrayList()
                for (supportedSize in supportedSizes) {
                    val size = Math.min(supportedSize.width, supportedSize.height)
                    if (size <= mAnalysisQuality) {
                        list.add(supportedSize)
                    }
                }
                list
            }
            .build()
    }
}