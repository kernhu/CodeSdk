package com.xcion.code.scanner.bean

/**
 * @date: 2024/1/22
 * @Description: 帧元数据
 */
class FrameMetadata(private val width: Int = 0, private val height: Int = 0, private val rotation: Int = 0) {


    /**
     * 帧元数据的宽
     *
     * @return
     */
    fun getWidth(): Int {
        return width
    }

    /**
     * 帧元数据的高
     *
     * @return
     */
    fun getHeight(): Int {
        return height
    }

    /**
     * 获取旋转角度
     *
     * @return
     */
    fun getRotation(): Int {
        return rotation
    }

    override fun toString(): String {
        return "FrameMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                '}'
    }
}