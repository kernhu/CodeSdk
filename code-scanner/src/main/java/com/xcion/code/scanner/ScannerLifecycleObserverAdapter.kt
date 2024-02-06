package com.xcion.code.scanner

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * @author: huming
 * @date: 2024/2/4
 * @Description:
 * This annotation required the usage of code generation or reflection, which should be avoided. Use DefaultLifecycleObserver or LifecycleEventObserver
 */
class ScannerLifecycleObserverAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val lifecycleObserver: ScannerLifecycleObserver,
) : LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public fun onCreate() {
        lifecycleObserver?.onCreate()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public fun onResume() {
        lifecycleObserver?.onResume()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public fun onPause() {
        lifecycleObserver?.onPause()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public fun onStop() {
        lifecycleObserver?.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public fun onDestroy() {
        lifecycleObserver?.onDestroy()
    }


}