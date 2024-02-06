package com.xcion.code.scanner.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.Nullable
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.xcion.code.scanner.CameraScan
import com.xcion.code.scanner.analyzer.AnalyzeResult
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.config.CameraConfig
import com.xcion.code.scanner.config.CameraConfigFactory
import com.xcion.code.scanner.manager.AmbientLightManager
import com.xcion.code.scanner.manager.AmbientLightManager.OnLightSensorEventListener
import com.xcion.code.scanner.manager.BeepManager
import com.xcion.code.scanner.utils.LogUtils
import com.xcion.code.scanner.utils.PermissionUtils
import java.util.concurrent.Executors


/**
 * @date: 2024/1/23
 * @Description: java类作用描述
 */
class BaseCameraScan<T>(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) : CameraScan<T>() {
    /**
     * Defines the maximum duration in milliseconds between a touch pad
     * touch and release for a given touch to be considered a tap (click) as
     * opposed to a hover movement gesture.
     */
    private val HOVER_TAP_TIMEOUT = 150

    /**
     * Defines the maximum distance in pixels that a touch pad touch can move
     * before being released for it to be considered a tap (click) as opposed
     * to a hover movement gesture.
     */
    private val HOVER_TAP_SLOP = 20

    /**
     * 每次缩放改变的步长
     */
    private val ZOOM_STEP_SIZE = 0.1f

    private var mContext: Context? = null
    private var mLifecycleOwner: LifecycleOwner? = null

    /**
     * 预览视图
     */
    private var mPreviewView: PreviewView? = null

    private var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    /**
     * 相机
     */
    private var mCamera: Camera? = null

    /**
     * 相机配置
     */
    private var mCameraConfig: CameraConfig? = null

    /**
     * 分析器
     */
    private var mAnalyzer: Analyzer<T>? = null

    /**
     * 是否分析
     */
    @Volatile
    private var isAnalyze = true

    /**
     * 是否自动停止分析
     */
    @Volatile
    private var isAutoStopAnalyze = true

    /**
     * 是否已经分析出结果
     */
    @Volatile
    private var isAnalyzeResult = false

    /**
     * 闪光灯（手电筒）视图
     */
    private var flashlightView: View? = null

    /**
     * 分析结果
     */
    private var mResultLiveData: MutableLiveData<AnalyzeResult<T>?>? = null

    /**
     * 扫描结果回调
     */
    private var mOnScanResultCallback: OnScanResultCallback<T>? = null

    /**
     * 分析监听器
     */
    private var mOnAnalyzeListener: Analyzer.OnAnalyzeListener<T>? = null

    /**
     * 音效管理器：主要用于播放蜂鸣提示音和振动效果
     */
    private var mBeepManager: BeepManager? = null

    /**
     * 环境光照度管理器：主要通过传感器来监听光照强度变化
     */
    private var mAmbientLightManager: AmbientLightManager? = null

    /**
     * 最后点击时间，根据两次点击时间间隔用于区分单机和触摸缩放事件
     */
    private var mLastHoveTapTime: Long = 0

    /**
     * 是否是点击事件
     */
    private var isClickTap = false

    /**
     * 按下时X坐标
     */
    private var mDownX = 0f

    /**
     * 按下时Y坐标
     */
    private var mDownY = 0f

    /**
     * 该代码必须在init 之前执行，否则会报空指针异常
     * 缩放手势检测
     */
    private var scaleGestureListener: OnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                val zoomState = getZoomState()
                if (zoomState != null) {
                    val ratio = zoomState.zoomRatio
                    // 根据缩放的手势和当前比例进行缩放
                    zoomTo(ratio * scale)
                    return true
                }
                return false
            }
        }


    init {
        mContext = context
        mLifecycleOwner = lifecycleOwner
        mPreviewView = previewView
        initData()
    }

    constructor(activity: ComponentActivity, previewView: PreviewView) : this(
        activity,
        activity,
        previewView
    )

    constructor(fragment: Fragment, previewView: PreviewView) : this(
        fragment.requireContext(),
        fragment.viewLifecycleOwner,
        previewView
    )

    @SuppressLint("ClickableViewAccessibility")
    private fun initData() {
        mResultLiveData = MutableLiveData()
        mResultLiveData!!.observe(
            mLifecycleOwner!!
        ) { result: AnalyzeResult<T>? ->
            if (result != null) {
                handleAnalyzeResult(result)
            } else if (mOnScanResultCallback != null) {
                mOnScanResultCallback!!.onScanResultFailure()
            }
        }
        mOnAnalyzeListener = object : Analyzer.OnAnalyzeListener<T> {
            override fun onSuccess(result: AnalyzeResult<T>) {
                mResultLiveData!!.postValue(result)
            }

            override fun onFailure(@Nullable e: Exception?) {
                mResultLiveData!!.postValue(null)
            }
        }


        var scaleGestureDetector = ScaleGestureDetector(mContext!!, scaleGestureListener)
        mPreviewView!!.setOnTouchListener { v, event ->
            event?.let { handlePreviewViewClickTap(it) }
            return@setOnTouchListener if (isNeedTouchZoom()) {
                scaleGestureDetector.onTouchEvent(event!!)
            } else {
                false
            }
        }

        mBeepManager = BeepManager(mContext?.applicationContext)
        mAmbientLightManager = mContext?.applicationContext?.let { AmbientLightManager(it) }
        mAmbientLightManager!!.register()
        mAmbientLightManager!!.setOnLightSensorEventListener(object : OnLightSensorEventListener {
            override fun onSensorChanged(lightLux: Float) {
                onSensorChanged(false, lightLux)
            }

            override fun onSensorChanged(dark: Boolean, lightLux: Float) {
                if (flashlightView != null) {
                    if (previewView?.visibility == View.VISIBLE) {
                        if (dark) {
                            if (flashlightView!!.visibility !== View.VISIBLE) {
                                flashlightView!!.visibility = View.VISIBLE
                                flashlightView!!.isSelected = isTorchEnabled()
                            }
                        } else if (flashlightView!!.visibility === View.VISIBLE && !isTorchEnabled()) {
                            flashlightView!!.visibility = View.INVISIBLE
                            flashlightView!!.isSelected = false
                        }
                    }
                }
            }
        })
    }

    /**
     * 处理预览视图点击事件；如果触发的点击事件被判定对焦操作，则开始自动对焦
     *
     * @param event 事件
     */
    private fun handlePreviewViewClickTap(event: MotionEvent) {
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClickTap = true
                    mDownX = event.x
                    mDownY = event.y
                    mLastHoveTapTime = System.currentTimeMillis()
                }

                MotionEvent.ACTION_MOVE -> isClickTap =
                    distance(mDownX, mDownY, event.x, event.y) < HOVER_TAP_SLOP

                MotionEvent.ACTION_UP -> if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                    // 开始对焦和测光
                    startFocusAndMetering(event.x, event.y)
                }
            }
        }
    }

    /**
     * 计算两点的距离
     *
     * @param aX a点X坐标
     * @param aY a点Y坐标
     * @param bX b点X坐标
     * @param bY b点Y坐标
     * @return 两点距离
     */
    private fun distance(aX: Float, aY: Float, bX: Float, bY: Float): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }

    /**
     * 开始对焦和测光
     *
     * @param x X轴坐标
     * @param y Y轴坐标
     */
    private fun startFocusAndMetering(x: Float, y: Float) {
        if (mCamera != null) {
            val point = mPreviewView!!.meteringPointFactory.createPoint(x, y)
            val focusMeteringAction = FocusMeteringAction.Builder(point).build()
            if (mCamera!!.cameraInfo.isFocusMeteringSupported(focusMeteringAction)) {
                mCamera!!.cameraControl.startFocusAndMetering(focusMeteringAction)
                LogUtils.d("startFocusAndMetering: $x,$y")
            }
        }
    }

    override fun setCameraConfig(cameraConfig: CameraConfig?): CameraScan<T>? {
        if (cameraConfig != null) {
            mCameraConfig = cameraConfig
        }
        return this
    }

    override fun startCamera() {
        if (mCameraConfig == null) {
            mCameraConfig = CameraConfigFactory.createDefaultCameraConfig(
                mContext,
                CameraSelector.LENS_FACING_UNKNOWN
            )
        }
        mCameraProviderFuture = ProcessCameraProvider.getInstance(mContext!!)
        mCameraProviderFuture!!.addListener({
            try {
                // 相机选择器
                val cameraSelector: CameraSelector? =
                    mCameraConfig?.options(CameraSelector.Builder())
                // 预览
                val preview: Preview = mCameraConfig!!.options(Preview.Builder())
                // 设置SurfaceProvider
                preview.setSurfaceProvider(mPreviewView!!.surfaceProvider)
                // 图像分析
                val imageAnalysis: ImageAnalysis = mCameraConfig!!.options(
                    ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                )
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { image: ImageProxy ->
                    if (isAnalyze && !isAnalyzeResult && mAnalyzer != null) {
                        mAnalyzer?.analyze(image, mOnAnalyzeListener!!)
                    }
                    image.close()
                }
                if (mCamera != null) {
                    mCameraProviderFuture?.get()?.unbindAll()
                }
                //绑定到生命周期
                mCamera = mCameraProviderFuture?.get()?.bindToLifecycle(
                    mLifecycleOwner!!,
                    cameraSelector!!, preview, imageAnalysis
                )
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }, ContextCompat.getMainExecutor(mContext!!))
    }

    /**
     * 处理分析结果
     *
     * @param result 分析结果
     */
    @Synchronized
    private fun handleAnalyzeResult(result: AnalyzeResult<T>) {
        if (isAnalyzeResult || !isAnalyze) {
            return
        }
        isAnalyzeResult = true
        if (isAutoStopAnalyze) {
            isAnalyze = false
        }
        if (mBeepManager != null) {
            mBeepManager!!.playBeepSoundAndVibrate()
        }
        if (mOnScanResultCallback != null) {
            mOnScanResultCallback!!.onScanResultCallback(result)
        }
        isAnalyzeResult = false
    }

    override fun stopCamera() {
        if (mCameraProviderFuture != null) {
            try {
                mCameraProviderFuture?.get()?.unbindAll()
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }


    override fun analyzeBitmap(bitmap: Bitmap) {
        Executors.newSingleThreadExecutor().execute {
            mOnAnalyzeListener?.let { mAnalyzer?.analyze(bitmap, it) }
        }
    }


    override fun setAnalyzeImage(analyze: Boolean): CameraScan<T>? {
        isAnalyze = analyze
        return this
    }

    override fun setAutoStopAnalyze(autoStopAnalyze: Boolean): CameraScan<T>? {
        isAutoStopAnalyze = autoStopAnalyze
        return this
    }

    override fun setAnalyzer(analyzer: Analyzer<T>?): CameraScan<T>? {
        mAnalyzer = analyzer
        return this
    }

    override fun zoomIn() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val ratio = zoomState.zoomRatio + ZOOM_STEP_SIZE
            val maxRatio = zoomState.maxZoomRatio
            if (ratio <= maxRatio) {
                mCamera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomOut() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val ratio = zoomState.zoomRatio - ZOOM_STEP_SIZE
            val minRatio = zoomState.minZoomRatio
            if (ratio >= minRatio) {
                mCamera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomTo(ratio: Float) {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val maxRatio = zoomState.maxZoomRatio
            val minRatio = zoomState.minZoomRatio
            val zoom = Math.max(Math.min(ratio, maxRatio), minRatio)
            mCamera!!.cameraControl.setZoomRatio(zoom)
        }
    }

    override fun lineZoomIn() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val zoom = zoomState.linearZoom + ZOOM_STEP_SIZE
            if (zoom <= 1f) {
                mCamera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomOut() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val zoom = zoomState.linearZoom - ZOOM_STEP_SIZE
            if (zoom >= 0f) {
                mCamera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float) {
        if (mCamera != null) {
            mCamera!!.cameraControl.setLinearZoom(linearZoom)
        }
    }

    override fun enableTorch(torch: Boolean) {
        if (mCamera != null && hasFlashUnit()) {
            mCamera!!.cameraControl.enableTorch(torch)
        }
    }

    override fun isTorchEnabled(): Boolean {
        if (mCamera != null) {
            val torchState = mCamera!!.cameraInfo.torchState.value
            return torchState != null && torchState == TorchState.ON
        }
        return false
    }

    override fun hasFlashUnit(): Boolean {
        return if (mCamera != null) {
            mCamera!!.cameraInfo.hasFlashUnit()
        } else mContext?.getPackageManager()
            ?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) == true
    }

    override fun setVibrate(vibrate: Boolean): CameraScan<T>? {
        if (mBeepManager != null) {
            mBeepManager!!.setVibrate(vibrate)
        }
        return this
    }

    override fun setPlayBeep(playBeep: Boolean): CameraScan<T>? {
        if (mBeepManager != null) {
            mBeepManager!!.setPlayBeep(playBeep)
        }
        return this
    }

    override fun setOnScanResultCallback(callback: OnScanResultCallback<T>?): CameraScan<T>? {
        mOnScanResultCallback = callback
        return this
    }

    @Nullable
    override fun getCamera(): Camera? {
        return mCamera
    }

    /**
     * 获取ZoomState
     *
     * @return [ZoomState]
     */
    @Nullable
    private fun getZoomState(): ZoomState? {
        return if (mCamera != null) {
            mCamera!!.cameraInfo.zoomState.value
        } else null
    }

    override fun release() {
        isAnalyze = false
        flashlightView = null
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.unregister()
        }
        if (mBeepManager != null) {
            mBeepManager!!.close()
        }
        stopCamera()
    }

    override fun bindFlashlightView(@Nullable flashlightView: View?): CameraScan<T>? {
        this.flashlightView = flashlightView
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.setLightSensorEnabled(flashlightView != null)
        }
        return this
    }

    override fun setDarkLightLux(lightLux: Float): CameraScan<T>? {
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.setDarkLightLux(lightLux)
        }
        return this
    }

    override fun setBrightLightLux(lightLux: Float): CameraScan<T>? {
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.setBrightLightLux(lightLux)
        }
        return this
    }


}