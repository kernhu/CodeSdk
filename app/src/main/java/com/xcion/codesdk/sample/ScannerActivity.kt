package com.xcion.codesdk.sample

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.xcion.code.scanner.CodeScannerActivity
import com.xcion.code.scanner.widget.ViewfinderView
import com.xcion.codesdk.R

/**
 * @date: 2024/1/22
 * @Description: sample for  scanner fragment
 */
class ScannerActivity : CodeScannerActivity() {

    override fun getHeaderLayoutId(): Int {
        return R.layout.custom_code_scanner_header
    }

    override fun getFooterLayoutId(): Int {
        return R.layout.custom_code_scanner_footer
    }

    override fun getSelectAlbumId(): Int {
        return R.id.footer_album
    }

    override fun getViewfinderColorId(): Int {
        return android.R.color.holo_orange_dark
    }

    override fun getViewfinderLaserStyle(): ViewfinderView.LaserStyle {
        return ViewfinderView.LaserStyle.GRID
    }

    override fun getBeepEnable(): Boolean {
        return super.getBeepEnable()
    }

    override fun getVibrateEnable(): Boolean {
        return super.getVibrateEnable()
    }

    override fun initHeaderView(header: View?) {
        super.initHeaderView(header)
        header?.findViewById<ImageView>(R.id.header_close)?.setOnClickListener {
            finish()
        }
        header?.findViewById<ImageView>(R.id.header_more)?.setOnClickListener {
            Toast.makeText(applicationContext, "you click more button", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initFooterView(header: View?) {
        super.initFooterView(header)
        header?.findViewById<ImageView>(R.id.footer_qrcode)?.setOnClickListener {
            Toast.makeText(applicationContext, "you click my arcode button", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onScanResult(content: String) {
        Toast.makeText(applicationContext, "Content:$content", Toast.LENGTH_SHORT).show()
        finish()
    }

}