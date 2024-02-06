package com.xcion.code.scanner

import androidx.lifecycle.LifecycleObserver

/**
 * @author: huming
 * @date: 2024/2/4
 * @Description: binding lifecycle
 */
interface ScannerLifecycleObserver : LifecycleObserver {

    fun onCreate()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onDestroy()

}