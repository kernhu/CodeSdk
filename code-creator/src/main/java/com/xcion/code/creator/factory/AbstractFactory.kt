package com.xcion.code.creator.factory

/**
 * @author: huming
 * @date: 2024/2/1
 * @Description: java类作用描述
 */
abstract class AbstractFactory : IFactory {

    companion object {

        const val BLACK = 0xff000000
        const val WHITE = 0xFFFFFFFF

        const val DEFAULT_QRCODE_WIDTH = 100

        const val DEFAULT_QRCODE_HEIGHT = 100

        const val DEFAULT_BARCODE_WIDTH = 100

        const val DEFAULT_BARCODE_HEIGHT = 60

    }

}