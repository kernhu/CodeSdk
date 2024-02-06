package com.xcion.code.scanner

import android.content.Intent

/**
 * @author: huming
 * @date: 2024/2/4
 * @Description: java类作用描述
 */
interface CodeScannerController {

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    )

    fun requestCameraPermissionResult(
        permissions: Array<String>,
        grantResults: IntArray
    )

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    )

}