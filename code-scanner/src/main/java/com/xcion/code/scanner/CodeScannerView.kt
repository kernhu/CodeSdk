package com.xcion.code.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import com.xcion.code.scanner.album.AlbumSelector
import com.xcion.code.scanner.analyzer.AnalyzeResult
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.analyzer.CodeScanningAnalyzer
import com.xcion.code.scanner.base.BaseCameraScan
import com.xcion.code.scanner.enums.CodeFormat
import com.xcion.code.scanner.utils.PermissionUtils
import com.xcion.code.scanner.utils.PointUtils
import com.xcion.code.scanner.widget.ViewfinderView


/**
 * @author: huming
 * @date: 2024/2/4
 * @Description: custom QRCode and Barcode scanner view
 */
open class CodeScannerView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes),
    ScannerLifecycleObserver,
    CodeScannerController,
    CameraScan.OnScanResultCallback<List<Barcode>> {

    private val CAMERA_PERMISSION_REQUEST_CODE =
        AlbumSelector.WRITE_EXTERNAL_STORAGE_REQUEST_CODE + 1

    private var codeFormat = 0
    private var viewfinderLaserStyle = 0
    private var headerLayoutId = 0
    private var footerLayoutId = 0
    private var selectAlbumId = 0
    private var customLaserDrawableId: Int? = null
    private var customFrameDrawableId: Int? = null
    private var viewfinderColorId = 0

    private var previewView: PreviewView? = null
    private var artworkView: ImageView? = null
    private var flashlightView: ImageView? = null
    private var viewfinderView: ViewfinderView? = null
    private var selectAlbumView: View? = null
    private var headerLayout: View? = null
    private var footerLayout: View? = null
    private var beepEnable: Boolean = true
    private var vibrateEnable: Boolean = true
    private var cameraScan: CameraScan<List<Barcode>>? = null
    private var callback: Callback? = null
    private var lifecycleObserverAdapter: ScannerLifecycleObserverAdapter? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var layoutInflater: LayoutInflater? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context) : this(context, null, 0, 0)

    init {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeScannerView)
        codeFormat = array.getInt(R.styleable.CodeScannerView_csvCodeFormat, 0)
        viewfinderLaserStyle = array.getInt(R.styleable.CodeScannerView_csvViewfinderLaserStyle, 0)
        headerLayoutId = array.getResourceId(R.styleable.CodeScannerView_csvHeaderLayoutId, 0)
        footerLayoutId = array.getResourceId(R.styleable.CodeScannerView_csvFooterLayoutId, 0)
        selectAlbumId = array.getResourceId(R.styleable.CodeScannerView_csvSelectAlbumId, 0)
        customLaserDrawableId =
            array.getResourceId(R.styleable.CodeScannerView_csvCustomLaserDrawableId, 0)
        customFrameDrawableId =
            array.getResourceId(R.styleable.CodeScannerView_csvCustomFrameDrawableId, 0)
        viewfinderColorId =
            array.getColor(R.styleable.CodeScannerView_csvViewfinderColorId, 0)
        beepEnable = array.getBoolean(R.styleable.CodeScannerView_csvBeepEnable, true)
        vibrateEnable = array.getBoolean(R.styleable.CodeScannerView_csvVibrateEnable, true)
        array.recycle()

        layoutInflater = LayoutInflater.from(context)

        initView()
        initCameraScan(createCameraScan(previewView))
        startCamera()
    }

    private fun initView() {

        artworkView = ImageView(context)
        artworkView?.scaleType = ImageView.ScaleType.CENTER_CROP
        this.addView(
            artworkView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        previewView = PreviewView(context)
        this.addView(
            previewView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        viewfinderView = ViewfinderView(context)
        this.addView(
            viewfinderView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        flashlightView = ImageView(context)
        flashlightView?.setImageDrawable(
            context.resources.getDrawable(
                R.drawable.code_scanner_flashlight_selector,
                null
            )
        )
        flashlightView?.setOnClickListener {
            toggleTorchState()
        }
        var flashlightLP = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        flashlightLP.gravity = Gravity.CENTER
        flashlightLP.topMargin =
            context.resources.getDimensionPixelSize(R.dimen.code_scanner_flashlight_margin_top)
        this.addView(
            flashlightView,
            flashlightLP
        )

        setHeaderLayoutId(headerLayoutId)
        setFooterLayoutId(footerLayoutId)
        setSelectAlbumId(selectAlbumId)

        if (customLaserDrawableId == View.NO_ID || customLaserDrawableId == View.NO_ID) {
            setViewfinderColorId(viewfinderColorId)
            setViewfinderLaserStyle(if (viewfinderLaserStyle == 0) ViewfinderView.LaserStyle.LINE else ViewfinderView.LaserStyle.GRID)
            viewfinderView?.setViewfinderStyle(ViewfinderView.ViewfinderStyle.CLASSIC)
        } else {
            viewfinderView?.setLaserStyle(ViewfinderView.LaserStyle.IMAGE)
            customLaserDrawableId?.let { setCustomLaserDrawableId(it) }
            customFrameDrawableId?.let { setCustomFrameDrawableId(it) }
        }

    }

    protected fun initCameraScan(cameraScan: CameraScan<List<Barcode>>) {
        this.cameraScan = cameraScan
        cameraScan.setAnalyzer(createAnalyzer())
            ?.bindFlashlightView(flashlightView)
            ?.setNeedTouchZoom(true)
            ?.setOnScanResultCallback(this)
        setBeepEnable(beepEnable)
        setVibrateEnable(vibrateEnable)
    }

    private fun createCameraScan(previewView: PreviewView?): CameraScan<List<Barcode>> {
        if (context is Fragment) {
            return BaseCameraScan(context as Fragment, previewView!!)
        } else {
            return BaseCameraScan(context as AppCompatActivity, previewView!!)
        }
    }

    private fun createAnalyzer(): Analyzer<List<Barcode>>? {
        return when (codeFormat) {
            CodeFormat.FORMATS_BARCODE.ordinal -> {
                CodeScanningAnalyzer(Barcode.FORMAT_ALL_FORMATS)
            }

            CodeFormat.FORMATS_QRCODE.ordinal -> {
                CodeScanningAnalyzer(Barcode.FORMAT_QR_CODE)
            }

            else -> {
                CodeScanningAnalyzer(Barcode.FORMAT_ALL_FORMATS)
            }
        }
    }

    /**
     * start preview camera
     */
    open fun startCamera() {
        if (cameraScan != null) {
            if (PermissionUtils.checkPermission(context, Manifest.permission.CAMERA)) {
                cameraScan!!.startCamera()
            } else {
                if (context is Fragment) {
                    PermissionUtils.requestPermission(
                        context as Fragment,
                        Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                } else {
                    PermissionUtils.requestPermission(
                        context as AppCompatActivity,
                        Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    /**
     * release camera
     */
    public fun releaseCamera() {
        if (cameraScan != null) {
            cameraScan!!.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            requestCameraPermissionResult(permissions, grantResults)
        } else if (requestCode == AlbumSelector.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (fragment != null) {
                AlbumSelector.with()
                    ?.pickOne(fragment!!)
            } else {
                AlbumSelector.with()
                    ?.pickOne(activity!!)
            }
        }
    }

    override fun requestCameraPermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            if (context is Activity) {
                (context as Activity).finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AlbumSelector.ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val inputStream = context.contentResolver.openInputStream(data!!.data!!)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    cameraScan?.analyzeBitmap(bitmap)
                    artworkView?.setImageBitmap(bitmap)
                    artworkView?.visibility = View.VISIBLE
                    previewView?.visibility = View.GONE
                }
            }
        }
    }

    public fun addLifecycleObserver(owner: LifecycleOwner): CodeScannerView {
        owner.let {
            lifecycleObserverAdapter = ScannerLifecycleObserverAdapter(owner, this)
            owner.lifecycle.addObserver(lifecycleObserverAdapter!!)
        }
        return this
    }

    public fun addCallback(callback: Callback): CodeScannerView {
        this.callback = callback
        return this
    }

    public fun build(activity: AppCompatActivity) {
        this.activity = activity
    }

    public fun build(fragment: Fragment) {
        this.fragment = fragment
    }

    public fun getHeaderLayout(): View? {
        return headerLayout
    }

    public fun getFooterLayout(): View? {
        return footerLayout
    }

    /**
     * open album and select a picture
     * */
    protected fun selectAlbum() {
        if (fragment != null) {
            AlbumSelector.with()
                ?.pickOne(fragment!!)
        } else {
            AlbumSelector.with()
                ?.pickOne(activity!!)
        }
        artworkView?.visibility = View.GONE
        previewView?.visibility = View.VISIBLE
        viewfinderView?.showResultPoints(arrayListOf())
        cameraScan?.setAnalyzeImage(true)
    }


    /**
     * switch flashlight can be use
     */
    protected open fun toggleTorchState() {
        if (cameraScan != null) {
            val isTorch = cameraScan!!.isTorchEnabled()
            cameraScan!!.enableTorch(!isTorch)
            flashlightView?.isSelected = !isTorch
        }
    }


    override fun onCreate() {

    }

    override fun onResume() {
        viewfinderView?.showScanner()
    }

    override fun onPause() {

    }

    override fun onStop() {

    }

    override fun onDestroy() {
        releaseCamera()
        lifecycleObserverAdapter?.let { lifecycleOwner?.lifecycle?.removeObserver(it) }
    }

    override fun onScanResultCallback(result: AnalyzeResult<List<Barcode>>) {
        cameraScan?.setAnalyzeImage(false)
        val barcodeList = result.getResult()
        val points: ArrayList<Point> = ArrayList()
        val width = result.getBitmapWidth()
        val height = result.getBitmapHeight()
        for (barcode in barcodeList) {
            val point: Point? = PointUtils.transform(
                barcode.boundingBox!!.centerX(),
                barcode.boundingBox!!.centerY(), width, height,
                viewfinderView!!.width, viewfinderView!!.height
            )
            point?.let { points.add(it) }
        }
        viewfinderView!!.showResultPoints(points)
        if (result.getResult().size === 1) {
            val barcode = result.getResult()[0]
            barcode?.let { it?.displayValue?.let { it1 -> callback?.onScanResult(it1) } }
        } else {
            viewfinderView!!.setOnItemClickListener(object : ViewfinderView.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val barcode = result.getResult()[position]
                    barcode?.let { it?.displayValue?.let { it1 -> callback?.onScanResult(it1) } }
                }
            })
        }
    }

    interface Callback {
        fun onScanResult(content: String)
    }


    public fun setCodeFormat(format: CodeFormat) {
        this.codeFormat = format.ordinal
        createAnalyzer()
    }

    public fun setViewfinderLaserStyle(style: ViewfinderView.LaserStyle) {
        viewfinderLaserStyle = style.ordinal
        viewfinderView?.setLaserStyle(if (viewfinderLaserStyle == 0) ViewfinderView.LaserStyle.LINE else ViewfinderView.LaserStyle.GRID)
    }


    public fun setHeaderLayoutId(layoutId: Int) {
        this.headerLayoutId = layoutId
        if (headerLayoutId != View.NO_ID && headerLayoutId != 0) {
            headerLayout = layoutInflater?.inflate(headerLayoutId, null, false)
        }
        var headerLP = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        headerLP.gravity = Gravity.TOP
        headerLayout?.let {
            this.addView(
                it,
                headerLP
            )
        }
    }

    public fun setFooterLayoutId(layoutId: Int) {
        this.footerLayoutId = layoutId
        if (footerLayoutId != View.NO_ID && footerLayoutId != 0) {
            footerLayout = layoutInflater?.inflate(footerLayoutId, null, false)
        }
        var footerLP = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        footerLP.gravity = Gravity.BOTTOM
        footerLayout?.let {
            this.addView(
                it,
                footerLP
            )
        }
    }

    public fun setSelectAlbumId(viewId: Int) {
        this.selectAlbumId = viewId
        if (selectAlbumId != View.NO_ID && selectAlbumId != 0) {
            selectAlbumView = findViewById<ImageView>(selectAlbumId)
            selectAlbumView?.setOnClickListener {
                selectAlbum()
            }
        }
    }

    public fun setViewfinderColorId(viewfinderColorId: Int) {
        this.viewfinderColorId = viewfinderColorId
        if (viewfinderColorId != 0) {
            viewfinderView?.setLaserColor(this.viewfinderColorId)
            viewfinderView?.setPointColor(this.viewfinderColorId)
            viewfinderView?.setFrameCornerColor(this.viewfinderColorId)
            viewfinderView?.setLabelTextColor(this.viewfinderColorId)
            viewfinderView?.setFrameColor(this.viewfinderColorId)
        }
    }

    public fun setCustomFrameDrawableId(customFrameDrawableId: Int) {
        this.customFrameDrawableId = customFrameDrawableId
        if (this.customFrameDrawableId != 0) {
            viewfinderView?.setFrameDrawable(this.customFrameDrawableId!!)
        }
    }

    public fun setCustomLaserDrawableId(customLaserDrawableId: Int) {
        this.customLaserDrawableId = customLaserDrawableId
        if (this.customLaserDrawableId != 0) {
            viewfinderView?.setLaserDrawable(this.customLaserDrawableId!!)
        }
    }

    public fun setVibrateEnable(enable: Boolean) {
        this.vibrateEnable = enable
        cameraScan?.setVibrate(vibrateEnable)
    }

    public fun setBeepEnable(enable: Boolean) {
        this.beepEnable = enable
        cameraScan?.setPlayBeep(beepEnable)
    }
}