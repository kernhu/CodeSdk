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
import com.xcion.code.scanner.CameraScan
import com.xcion.code.scanner.album.AlbumSelector
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.utils.PermissionUtils
import com.xcion.code.scanner.widget.ViewfinderView


/**
 * @date: 2024/1/23
 * @Description: activity 基类
 */
abstract class BaseCodeScannerActivity<T> : AppCompatActivity(),
    CameraScan.OnScanResultCallback<T> {

    private val CAMERA_PERMISSION_REQUEST_CODE =
        AlbumSelector.WRITE_EXTERNAL_STORAGE_REQUEST_CODE + 1

    protected var viewfinderView: ViewfinderView? = null
    protected var previewView: PreviewView? = null
    protected var flashlightView: View? = null
    protected var artworkView: ImageView? = null
    protected var headerView: View? = null
    protected var footerView: View? = null

    /**
     * CameraScan
     */
    private var mCameraScan: CameraScan<T>? = null


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isContentView()) {
            setContentView(getContentLayoutId())
        }
        initUI()
    }

    /**
     * 初始化
     */
    open fun initUI() {
        previewView = findViewById(getPreviewViewId())
        viewfinderView = findViewById(getViewfinderId())
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

        viewfinderView?.setLaserColor(getColor(getViewfinderColorId()))
        viewfinderView?.setPointColor(getColor(getViewfinderColorId()))
        viewfinderView?.setFrameCornerColor(getColor(getViewfinderColorId()))
        viewfinderView?.setLabelTextColor(getColor(getViewfinderColorId()))
        viewfinderView?.setFrameColor(getColor(getViewfinderColorId()))


        val ivFlashlightId = getFlashlightId()
        if (ivFlashlightId != View.NO_ID && ivFlashlightId != 0) {
            flashlightView = findViewById(ivFlashlightId)
            if (flashlightView != null) {
                flashlightView!!.visibility = View.VISIBLE
                flashlightView!!.setOnClickListener { v -> onClickFlashlight() }
            }
        }

        val artworkViewId = getFlashlightId()
        if (artworkViewId != View.NO_ID && artworkViewId != 0) {
            artworkView = findViewById(getArtworkId())
        }

        mCameraScan = createCameraScan(previewView)
        initCameraScan(mCameraScan!!)
        startCamera()

        val headerLayoutId = getHeaderLayoutId()
        val footerLayoutId = getFooterLayoutId()
        if (headerLayoutId != View.NO_ID && headerLayoutId != 0) {
            headerView = LayoutInflater.from(this).inflate(headerLayoutId, null, false)
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
            footerView = LayoutInflater.from(this).inflate(footerLayoutId, null, false)
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
            findViewById<View>(selectAlbumId).setOnClickListener {
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
            ?.setVibrate(getVibrateEnable())
            ?.setPlayBeep(getBeepEnable())
            ?.bindFlashlightView(flashlightView)
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
            flashlightView?.isSelected = !isTorch
        }
    }

    /**
     * 启动相机预览
     */
    open fun startCamera() {
        if (mCameraScan != null) {
            if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
                mCameraScan!!.startCamera()
            } else {
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
    public fun releaseCamera() {
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
        } else if (requestCode == AlbumSelector.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            AlbumSelector.with()?.pickOne(this)
        }
    }

    open fun requestCameraPermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AlbumSelector.ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val inputStream = contentResolver.openInputStream(data!!.data!!)
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

    override fun onResume() {
        super.onResume()
        viewfinderView?.showScanner()
    }


    override fun onDestroy() {
        releaseCamera()
        super.onDestroy()
    }

    open fun isContentView(): Boolean {
        return true
    }

    abstract fun getContentLayoutId(): Int

    abstract fun getPreviewViewId(): Int

    abstract fun getFlashlightId(): Int

    abstract fun getViewfinderId(): Int

    abstract fun getSelectAlbumId(): Int

    abstract fun getHeaderLayoutId(): Int

    abstract fun getFooterLayoutId(): Int

    abstract fun getArtworkId(): Int

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

    open fun createCameraScan(previewView: PreviewView?): CameraScan<T> {
        return BaseCameraScan(this, previewView!!)
    }

    @Nullable
    abstract fun createAnalyzer(): Analyzer<T>?

}