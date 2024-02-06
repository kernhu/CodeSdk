package com.xcion.code.scanner.analyzer

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.BarcodeFormat
import com.google.mlkit.vision.common.InputImage


/**
 * @date: 2024/1/23
 * @Description: java类作用描述
 */
class CodeScanningAnalyzer(options: BarcodeScannerOptions?) : CommonAnalyzer<List<Barcode>>() {

    private var mDetector: BarcodeScanner? = null

    constructor(
        @BarcodeFormat barcodeFormat: Int,
        @BarcodeFormat vararg barcodeFormats: Int
    ) : this(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(barcodeFormat, *barcodeFormats)
            .build()
    )


    init {
        mDetector = if (options != null) {
            BarcodeScanning.getClient(options)
        } else {
            BarcodeScanning.getClient()
        }
    }

    override fun detectInImage(inputImage: InputImage?): Task<List<Barcode?>?> {
        return mDetector!!.process(inputImage!!)
    }

}