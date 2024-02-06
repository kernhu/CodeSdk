package com.xcion.code.scanner.base

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import com.xcion.code.scanner.CameraScan
import com.xcion.code.scanner.album.AlbumSelector
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.utils.LogUtils
import com.xcion.code.scanner.utils.PermissionUtils
import com.xcion.code.scanner.widget.ViewfinderView


/**
 * @date: 2024/1/23
 * @Description: fragment 基类
 */
abstract class BaseCodeScannerFragment<T> : Fragment(), CameraScan.OnScanResultCallback<T> {

    /**
     * 相机权限请求代码
     */
    private val CAMERA_PERMISSION_REQUEST_CODE = 0x1024

    private var rootView: View? = null
    protected var viewfinderView: ViewfinderView? = null
    protected var previewView: PreviewView? = null
    protected var flashlightView: View? = null
    protected var artworkView: ImageView? = null
    protected var headerView: View? = null
    protected var footerView: View? = null

    /**
     * CameraScan
     */
    var mCameraScan: CameraScan<T>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isContentView()) {
            rootView = createRootView(inflater, container)
        }
        return rootView
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    /**
     * 初始化
     */
    open fun initUI() {
        previewView = rootView?.findViewById(getPreviewViewId())
        viewfinderView = rootView?.findViewById(getViewfinderId())

        viewfinderView?.setLaserStyle(getViewfinderLaserStyle())
        viewfinderView?.setViewfinderStyle(ViewfinderView.ViewfinderStyle.CLASSIC)
        var customLaserDrawableId = getCustomLaserDrawableId()
        var customFrameDrawableId = getCustomFrameDrawableId()
        if (customLaserDrawableId != View.NO_ID && customLaserDrawableId != 0) {
            viewfinderView?.setLaserDrawable(customLaserDrawableId)
        }
        if (customFrameDrawableId != View.NO_ID && customFrameDrawableId != 0) {
            viewfinderView?.setFrameDrawable(customFrameDrawableId)
        }
        viewfinderView?.setLaserColor(requireActivity().getColor(getViewfinderColorId()))
        viewfinderView?.setPointColor(requireActivity().getColor(getViewfinderColorId()))
        viewfinderView?.setFrameCornerColor(requireActivity().getColor(getViewfinderColorId()))
        viewfinderView?.setLabelTextColor(requireActivity().getColor(getViewfinderColorId()))
        viewfinderView?.setFrameColor(requireActivity().getColor(getViewfinderColorId()))

        val ivFlashlightId = getFlashlightId()
        if (ivFlashlightId != View.NO_ID && ivFlashlightId != 0) {
            flashlightView = rootView?.findViewById(ivFlashlightId)
            if (flashlightView != null) {
                flashlightView!!.setOnClickListener { v -> onClickFlashlight() }
            }
        }
        val artworkViewId = getFlashlightId()
        if (artworkViewId != View.NO_ID && artworkViewId != 0) {
            artworkView = rootView?.findViewById(getArtworkId())
        }

        mCameraScan = createCameraScan(previewView)
        initCameraScan(mCameraScan!!)
        startCamera()

        val headerLayoutId = getHeaderLayoutId()
        val footerLayoutId = getFooterLayoutId()
        if (headerLayoutId != View.NO_ID && headerLayoutId != 0) {
            headerView = LayoutInflater.from(activity).inflate(headerLayoutId, null, false)
            var parentView = previewView?.parent as ViewGroup
            var lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            parentView.addView(headerView, lp)
            if (headerView?.layoutParams is FrameLayout.LayoutParams) {
                var layoutParams = (headerView?.layoutParams as FrameLayout.LayoutParams)
                layoutParams.gravity = Gravity.TOP
                headerView?.layoutParams = layoutParams
            } else {
                throw IllegalAccessException("the root layout must be framelayout")
            }
            initHeaderView(headerView!!)
        }
        if (footerLayoutId != View.NO_ID && footerLayoutId != 0) {
            footerView = LayoutInflater.from(activity).inflate(footerLayoutId, null, false)
            var parentView = previewView?.parent as ViewGroup
            var lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            parentView.addView(footerView, lp)
            if (footerView?.layoutParams is FrameLayout.LayoutParams) {
                var layoutParams = (footerView?.layoutParams as FrameLayout.LayoutParams)
                layoutParams.gravity = Gravity.BOTTOM
                footerView?.layoutParams = layoutParams
            } else {
                throw IllegalAccessException("the root layout must be framelayout")
            }
            initFooterView(footerView!!)
        }

        val selectAlbumId = getSelectAlbumId()
        if (selectAlbumId != View.NO_ID && selectAlbumId != 0) {
            rootView?.findViewById<View>(selectAlbumId)?.setOnClickListener {
                AlbumSelector.with()?.pickOne(this)
                artworkView?.visibility = View.GONE
                previewView?.visibility = View.VISIBLE
                viewfinderView?.showResultPoints(arrayListOf())
                getCameraScan()?.setAnalyzeImage(true)
            }
        }
    }

    /**
     * 初始化CameraScan
     */
    open fun initCameraScan(cameraScan: CameraScan<T>) {
        cameraScan.setAnalyzer(createAnalyzer())
            ?.bindFlashlightView(flashlightView)
            ?.setVibrate(getVibrateEnable())
            ?.setPlayBeep(getBeepEnable())
            ?.setOnScanResultCallback(this)
    }

    /**
     * 点击手电筒
     */
    protected open fun onClickFlashlight() {
        toggleTorchState()
    }

    /**
     * 切换闪光灯状态（开启/关闭）
     */
    protected open fun toggleTorchState() {
        if (getCameraScan() != null) {
            val isTorch = getCameraScan()!!.isTorchEnabled()
            getCameraScan()!!.enableTorch(!isTorch)
            if (flashlightView != null) {
                flashlightView!!.isSelected = !isTorch
            }
        }
    }

    /**
     * 启动相机预览
     */
    open fun startCamera() {
        if (mCameraScan != null) {
            if (PermissionUtils.checkPermission(requireContext(), Manifest.permission.CAMERA)) {
                mCameraScan!!.startCamera()
            } else {
                LogUtils.d("checkPermissionResult != PERMISSION_GRANTED")
                PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.CAMERA,
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * 释放相机
     */
    public open fun releaseCamera() {
        if (mCameraScan != null) {
            mCameraScan!!.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            requestCameraPermissionResult(permissions, grantResults)
        }
    }

    /**
     * 请求Camera权限回调结果
     *
     * @param permissions  权限
     * @param grantResults 授权结果
     */
    open fun requestCameraPermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        viewfinderView?.showScanner()
    }

    override fun onDestroyView() {
        releaseCamera()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AlbumSelector.ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val inputStream = requireActivity().contentResolver.openInputStream(data!!.data!!)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    mCameraScan?.analyzeBitmap(bitmap)
                    artworkView?.setImageBitmap(bitmap)
                    artworkView?.visibility = View.VISIBLE
                    previewView?.visibility = View.GONE
                }
            }
        }
    }


    open fun isContentView(): Boolean {
        return true
    }

    open fun createRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(getContentLayoutId(), container, false)
    }

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    abstract fun getContentLayoutId(): Int

    /**
     * 预览视图[.previewView]的ID
     *
     * @return 预览视图ID
     */
    abstract fun getPreviewViewId(): Int

    /***
     * viewfinder rescue id
     * */
    abstract fun getViewfinderId(): Int

    abstract fun getFlashlightId(): Int

    abstract fun getArtworkId(): Int

    abstract fun getSelectAlbumId(): Int

    /***
     * header view
     * */
    abstract fun getHeaderLayoutId(): Int

    /***
     * footer view
     * */
    abstract fun getFooterLayoutId(): Int

    abstract fun initHeaderView(header: View?)

    abstract fun initFooterView(header: View?)

    abstract fun getViewfinderColorId(): Int

    abstract fun getViewfinderLaserStyle(): ViewfinderView.LaserStyle

    abstract fun getCustomLaserDrawableId(): Int

    abstract fun getCustomFrameDrawableId(): Int

    abstract fun getVibrateEnable(): Boolean

    abstract fun getBeepEnable(): Boolean

    open fun getCameraScan(): CameraScan<T>? {
        return mCameraScan
    }

    /**
     * 获取根视图
     *
     * @return [.mRootView]
     */
    open fun getRootView(): View? {
        return rootView
    }

    /**
     * 创建[CameraScan]
     *
     * @param previewView [PreviewView]
     * @return [CameraScan]
     */
    open fun createCameraScan(previewView: PreviewView?): CameraScan<T> {
        return BaseCameraScan(this, previewView!!)
    }

    /**
     * 创建分析器
     *
     * @return [Analyzer]
     */
    @Nullable
    abstract fun createAnalyzer(): Analyzer<T>?


}