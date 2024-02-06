package com.xcion.code.scanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.camera.core.CameraSelector
import com.xcion.code.scanner.analyzer.AnalyzeResult
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.config.CameraConfig


/**
 * @date: 2024/1/23
 * @Description: 相机扫描基类定义；内置的默认实现见：{@link BaseCameraScan}
 *  * 快速实现扫描识别主要有以下几种方式：
 * <p>
 * 1、通过继承 {@link BaseCameraScanActivity}或者{@link BaseCameraScanFragment}或其子类，可快速实现扫描识别。
 * （适用于大多数场景，自定义布局时需覆写getLayoutId方法）
 * <p>
 * 2、在你项目的Activity或者Fragment中实例化一个{@link BaseCameraScan}。（适用于想在扫描界面写交互逻辑，又因为项目
 * 架构或其它原因，无法直接或间接继承{@link BaseCameraScanActivity}或{@link BaseCameraScanFragment}时使用）
 * <p>
 * 3、继承{@link CameraScan}自己实现一个，可参照默认实现类{@link BaseCameraScan}，其他步骤同方式2。（高级用法，谨慎使用）
 *
 */
abstract class CameraScan<T> : ICamera, ICameraControl ,IBitmapControl{


    /**
     * 扫描返回结果的key；解析方式可参见：[.parseScanResult]
     */
    var SCAN_RESULT = "SCAN_RESULT"

    /**
     * A camera on the device facing the same direction as the device's screen.
     */
    var LENS_FACING_FRONT = CameraSelector.LENS_FACING_FRONT

    /**
     * A camera on the device facing the opposite direction as the device's screen.
     */
    var LENS_FACING_BACK = CameraSelector.LENS_FACING_BACK

    companion object {
        /**
         * 纵横比：4:3
         */
        val ASPECT_RATIO_4_3 = 4.0f / 3.0f

        /**
         * 纵横比：16:9
         */
        val ASPECT_RATIO_16_9 = 16.0f / 9.0f

    }

    /**
     * 是否需要支持触摸缩放
     */
    private var isNeedTouchZoom = true

    /**
     * 扩展参数
     */
    protected var mExtras: Bundle? = null

    /**
     * 是否需要支持触摸缩放
     *
     * @return 返回是否需要支持触摸缩放
     */
    protected open fun isNeedTouchZoom(): Boolean {
        return isNeedTouchZoom
    }

    /**
     * 设置是否需要支持触摸缩放
     *
     * @param needTouchZoom 是否需要支持触摸缩放
     * @return [CameraScan]
     */
    open fun setNeedTouchZoom(needTouchZoom: Boolean): CameraScan<T>? {
        isNeedTouchZoom = needTouchZoom
        return this
    }

    /**
     * 获取扩展参数：当[CameraScan]的默认实现不满足你的需求时，你可以通过自定义实现一个[CameraScan]；
     * 然后通过此方法获取扩展参数，进行扩展参数的传递；需使用时直接在实现类中获取 [.mExtras]即可。
     *
     * @return [Bundle]
     */
    open fun getExtras(): Bundle {
        if (mExtras == null) {
            mExtras = Bundle()
        }
        return mExtras as Bundle
    }

    /**
     * 设置相机配置，请在[.startCamera]之前调用
     *
     * @param cameraConfig 相机配置
     * @return [CameraScan]
     */
    abstract fun setCameraConfig(cameraConfig: CameraConfig?): CameraScan<T>?

    /**
     * 设置是否分析图像，默认为：true；通过此方法可以动态控制是否分析图像；在连续扫描识别时，可能会用到。
     *
     *
     * 如：当分析图像成功一次之后，如需继续连扫，可以在结果回调函数中等处理了自己的业务后，继续调用此方法并设置为true，就可以继续扫描分析图像了。
     *
     * @param analyze 是否分析图像
     * @return [CameraScan]
     */
    abstract fun setAnalyzeImage(analyze: Boolean): CameraScan<T>?

    /**
     * 设置是否自动停止分析图像；默认为：true；
     *
     *
     * 大多数情况下，单次扫描的场景应用较多；很容易忘记主动调用 [CameraScan.setAnalyzeImage] 来停止分析。
     *
     *
     * 如果设置为：true；即：启用了自动停止分析图像：当分析图像成功一次之后；那么设置的分析图像会自动停止；如果此时
     * 需要继续分析图像，可以在结果回调里面调用 [CameraScan.setAnalyzeImage] 来控制是否继续分析图像。
     *
     *
     * 如果设置为：false；即：禁用了自动停止分析图像：当分析图像成功一次之后；不会有任何变化；会继续分析图像。
     *
     * @param autoStopAnalyze
     * @return
     */
    abstract fun setAutoStopAnalyze(autoStopAnalyze: Boolean): CameraScan<T>?

    /**
     * 设置分析器，如果内置的一些分析器不满足您的需求，你也可以自定义[Analyzer]，
     * 自定义时，切记需在[.startCamera]之前调用才有效。
     *
     * @param analyzer 分析器
     * @return [CameraScan]
     */
    abstract fun setAnalyzer(analyzer: Analyzer<T>?): CameraScan<T>?

    /**
     * 设置是否振动
     *
     * @param vibrate 是否振动
     * @return [CameraScan]
     */
    abstract fun setVibrate(vibrate: Boolean): CameraScan<T>?

    /**
     * 设置是否播放提示音
     *
     * @param playBeep 是否播放蜂鸣提示音
     * @return [CameraScan]
     */
    abstract fun setPlayBeep(playBeep: Boolean): CameraScan<T>?

    /**
     * 设置扫描结果回调
     *
     * @param callback 扫描结果回调
     * @return [CameraScan]
     */
    abstract fun setOnScanResultCallback(callback: OnScanResultCallback<T>?): CameraScan<T>?

    /**
     * 绑定手电筒，绑定后可根据光照传感器，动态显示或隐藏手电筒；并自动处理点击手电筒时的开关切换。
     *
     * @param v 手电筒视图
     * @return [CameraScan]
     */
    abstract fun bindFlashlightView(@Nullable v: View?): CameraScan<T>?

    /**
     * 设置光照强度足够暗的阈值（单位：lux），需要通过[.bindFlashlightView]绑定手电筒才有效
     *
     * @param lightLux 光照度阈值
     * @return [CameraScan]
     */
    abstract fun setDarkLightLux(lightLux: Float): CameraScan<T>?

    /**
     * 设置光照强度足够明亮的阈值（单位：lux），需要通过[.bindFlashlightView]绑定手电筒才有效
     *
     * @param lightLux 光照度阈值
     * @return [CameraScan]
     */
    abstract fun setBrightLightLux(lightLux: Float): CameraScan<T>?

    /**
     * 扫描结果回调
     *
     * @param <T> 扫描结果数据类型
    </T> */
    interface OnScanResultCallback<T> {
        /**
         * 扫描结果回调
         *
         * @param result 扫描结果
         */
        fun onScanResultCallback(result: AnalyzeResult<T>)

        /**
         * 扫描结果识别失败时触发此回调方法
         */
        fun onScanResultFailure(){

        }
    }

    /**
     * 解析扫描结果
     *
     * @param data 需解析的意图数据
     * @return 返回解析结果
     */
    @Nullable
    open fun parseScanResult(data: Intent?): String? {
        return data?.getStringExtra(SCAN_RESULT)
    }


}