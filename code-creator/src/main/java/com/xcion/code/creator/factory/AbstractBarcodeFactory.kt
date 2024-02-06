package com.xcion.code.creator.factory

import com.google.zxing.BarcodeFormat

/**
 * @author: huming
 * @date: 2024/2/1
 * @Description: 条形码工厂类
 */
abstract class AbstractBarcodeFactory : AbstractFactory() {


    protected val barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128

}