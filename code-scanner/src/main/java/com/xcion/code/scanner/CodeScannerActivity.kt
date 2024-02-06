package com.xcion.code.scanner

import android.graphics.Point
import android.view.View
import com.google.mlkit.vision.barcode.common.Barcode
import com.xcion.code.scanner.analyzer.AnalyzeResult
import com.xcion.code.scanner.analyzer.Analyzer
import com.xcion.code.scanner.analyzer.CodeScanningAnalyzer
import com.xcion.code.scanner.base.BaseCodeScannerActivity
import com.xcion.code.scanner.constants.ArgsKey
import com.xcion.code.scanner.enums.CodeFormat
import com.xcion.code.scanner.utils.PointUtils
import com.xcion.code.scanner.widget.ViewfinderView

/**
 * @author KernHu
 * @date: 2024/1/23
 * @Description: 扫码基类
 */
abstract class CodeScannerActivity : BaseCodeScannerActivity<List<Barcode>>() {

    override fun getContentLayoutId(): Int {
        return R.layout.code_scanner_layout
    }

    override fun getPreviewViewId(): Int {
        return R.id.code_scanner_preview_view
    }

    override fun getFlashlightId(): Int {
        return R.id.code_scanner_flashlight
    }

    override fun getViewfinderId(): Int {
        return R.id.code_scanner_viewfinder_view
    }

    override fun getArtworkId(): Int {
        return R.id.code_scanner_artwork
    }

    override fun getHeaderLayoutId(): Int {
        return View.NO_ID
    }

    override fun getFooterLayoutId(): Int {
        return View.NO_ID
    }

    override fun getSelectAlbumId(): Int {
        return View.NO_ID
    }

    override fun getViewfinderColorId(): Int {
        return android.R.color.holo_blue_bright
    }

    override fun getViewfinderLaserStyle(): ViewfinderView.LaserStyle {
        return ViewfinderView.LaserStyle.LINE
    }

    override fun getCustomLaserDrawableId(): Int {
        return View.NO_ID
    }

    override fun getCustomFrameDrawableId(): Int {
        return View.NO_ID
    }

    override fun getBeepEnable(): Boolean {
       return true
    }

    override fun getVibrateEnable(): Boolean {
        return true
    }

    override fun initHeaderView(header: View?) {

    }

    override fun initFooterView(header: View?) {

    }

    override fun initCameraScan(cameraScan: CameraScan<List<Barcode>>) {
        super.initCameraScan(cameraScan)

    }

    override fun createAnalyzer(): Analyzer<List<Barcode>>? {
        var format = intent.getIntExtra(ArgsKey.KEY_FORMATS, CodeFormat.FORMATS_ALL.ordinal)
        return when (format) {
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

    override fun onScanResultCallback(result: AnalyzeResult<List<Barcode>>) {
        getCameraScan()?.setAnalyzeImage(false)
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
            barcode?.let { onScanResult(it) }
        } else {
            viewfinderView!!.setOnItemClickListener(object : ViewfinderView.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val barcode = result.getResult()[position]
                    barcode?.let { onScanResult(it) }
                }
            })
        }
    }

    private fun onScanResult(barcode: Barcode) {
        barcode.displayValue?.let { onScanResult(it) }
    }

    abstract fun onScanResult(content: String)
}