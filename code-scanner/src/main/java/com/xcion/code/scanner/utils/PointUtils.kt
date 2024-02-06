package com.xcion.code.scanner.utils

import android.graphics.Point
import java.util.Locale

/**
 * @date: 2024/1/30
 * @Description: Point工具类
 */
class PointUtils {

    companion object {

        /**
         * 转换坐标：将原始 point 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
         *
         * @param point      原始坐标点
         * @param srcWidth   原始宽度
         * @param srcHeight  原始高度
         * @param destWidth  目标宽度
         * @param destHeight 目标高度
         * @return 转换之后的坐标点
         * @return
         */
        fun transform(
            point: Point,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int
        ): Point? {
            return transform(point, srcWidth, srcHeight, destWidth, destHeight, false)
        }

        /**
         * 转换坐标：将原始 point 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
         *
         * @param point      原始坐标点
         * @param srcWidth   原始宽度
         * @param srcHeight  原始高度
         * @param destWidth  目标宽度
         * @param destHeight 目标高度
         * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
         * @return 转换之后的坐标点
         */
        fun transform(
            point: Point,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int,
            isFit: Boolean
        ): Point? {
            return transform(point.x, point.y, srcWidth, srcHeight, destWidth, destHeight, isFit)
        }

        /**
         * 转换坐标：将原始 x，y 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
         *
         * @param x          原始X坐标
         * @param y          原值Y坐标
         * @param srcWidth   原始宽度
         * @param srcHeight  原始高度
         * @param destWidth  目标宽度
         * @param destHeight 目标高度
         * @return 转换之后的坐标点
         */
        fun transform(
            x: Int,
            y: Int,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int
        ): Point? {
            return transform(x, y, srcWidth, srcHeight, destWidth, destHeight, false)
        }

        /**
         * 转换坐标：将原始 x，y 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
         *
         * @param x          原始X坐标
         * @param y          原值Y坐标
         * @param srcWidth   原始宽度
         * @param srcHeight  原始高度
         * @param destWidth  目标宽度
         * @param destHeight 目标高度
         * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
         * @return 转换之后的坐标点
         */
        fun transform(
            x: Int,
            y: Int,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int,
            isFit: Boolean
        ): Point? {
            LogUtils.d(
                String.format(
                    Locale.getDefault(),
                    "transform: %d,%d | %d,%d",
                    srcWidth,
                    srcHeight,
                    destWidth,
                    destHeight
                )
            )
            val widthRatio = destWidth * 1.0f / srcWidth
            val heightRatio = destHeight * 1.0f / srcHeight
            val point = Point()
            if (isFit) {
                // 宽或高自适应铺满
                val ratio = Math.min(widthRatio, heightRatio)
                val left = Math.abs(srcWidth * ratio - destWidth) / 2
                val top = Math.abs(srcHeight * ratio - destHeight) / 2
                point.x = (x * ratio + left).toInt()
                point.y = (y * ratio + top).toInt()
            } else {
                // 填充铺满（可能会出现裁剪）
                val ratio = Math.max(widthRatio, heightRatio)
                val left = Math.abs(srcWidth * ratio - destWidth) / 2
                val top = Math.abs(srcHeight * ratio - destHeight) / 2
                point.x = (x * ratio - left).toInt()
                point.y = (y * ratio - top).toInt()
            }
            return point
        }
    }
}