package com.xcion.code.scanner.manager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


/**
 * @date: 2024/1/23
 * @Description: 环境光照度管理器：主要通过传感器来监听光照强度变化
 */
class AmbientLightManager(context: Context) : SensorEventListener {


    private val INTERVAL_TIME = 200

    protected val DARK_LUX = 45.0f
    protected val BRIGHT_LUX = 100.0f

    /**
     * 光照度太暗时，默认：光照度 45 lux
     */
    private var darkLightLux = DARK_LUX

    /**
     * 光照度足够亮时，默认：光照度 100 lux
     */
    private var brightLightLux = BRIGHT_LUX

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null

    private var lastTime: Long = 0

    private var isLightSensorEnabled = false

    private var mOnLightSensorEventListener: OnLightSensorEventListener? = null

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        isLightSensorEnabled = true
    }

    /**
     * 注册
     */
    fun register() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager!!.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * 注销
     */
    fun unregister() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager!!.unregisterListener(this)
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (isLightSensorEnabled) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime < INTERVAL_TIME) {
                // 降低频率
                return
            }
            lastTime = currentTime
            if (mOnLightSensorEventListener != null) {
                val lightLux = sensorEvent.values[0]
                mOnLightSensorEventListener!!.onSensorChanged(lightLux)
                if (lightLux <= darkLightLux) {
                    mOnLightSensorEventListener!!.onSensorChanged(true, lightLux)
                } else if (lightLux >= brightLightLux) {
                    mOnLightSensorEventListener!!.onSensorChanged(false, lightLux)
                }
            }
        }
    }

    /**
     * 设置光照强度足够暗的阈值（单位：lux）
     *
     * @param lightLux
     */
    fun setDarkLightLux(lightLux: Float) {
        darkLightLux = lightLux
    }

    /**
     * 设置光照强度足够明亮的阈值（单位：lux）
     *
     * @param lightLux
     */
    fun setBrightLightLux(lightLux: Float) {
        brightLightLux = lightLux
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }

    fun isLightSensorEnabled(): Boolean {
        return isLightSensorEnabled
    }

    /**
     * 设置是否启用光照传感器
     *
     * @param lightSensorEnabled
     */
    fun setLightSensorEnabled(lightSensorEnabled: Boolean) {
        isLightSensorEnabled = lightSensorEnabled
    }

    /**
     * 设置光照传感器监听器，只有在 [.isLightSensorEnabled] 为`true` 才有效
     *
     * @param listener
     */
    fun setOnLightSensorEventListener(listener: OnLightSensorEventListener?) {
        mOnLightSensorEventListener = listener
    }

    open interface OnLightSensorEventListener {
        /**
         * @param lightLux 当前检测到的光照强度值
         */
        fun onSensorChanged(lightLux: Float)

        /**
         * 传感器改变事件
         *
         * @param dark     是否太暗了，当检测到的光照强度值小于[.darkLightLux]时，为`true`
         * @param lightLux 当前检测到的光照强度值
         */
        fun onSensorChanged(dark: Boolean, lightLux: Float)
    }
}